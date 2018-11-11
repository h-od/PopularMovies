package com.hughod.movies.util

import android.content.Context
import android.net.ConnectivityManager
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module
import org.reactivestreams.Publisher
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

val dataUtils = module {
    single { ErrorHandler.Impl(get()) as ErrorHandler }
    single { RetryHandler.Impl(get()) as RetryHandler }
    single { Connectivity(androidApplication()) }
}

interface ErrorHandler {
    fun <T> handle(onNetworkError: Consumer<Throwable>?, onDataError: Consumer<Throwable>?): SingleTransformer<T, T>

    class Impl(private val retryHandler: RetryHandler) : ErrorHandler {
        override fun <T> handle(onNetworkError: Consumer<Throwable>?, onDataError: Consumer<Throwable>?): SingleTransformer<T, T> =
                SingleTransformer { singleStream ->
                    singleStream
                            .onErrorResumeNext { throwable -> Single.error(NetworkException(throwable)) }
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(handleError(onNetworkError, onDataError))
                            .retryWhen(retryHandler.retryWithConnectivity())
                }

        private fun handleError(onNetworkError: Consumer<Throwable>?, onDataError: Consumer<Throwable>?): Consumer<Throwable> =
                Consumer { throwable ->
                    if (throwable is NetworkException) {
                        when (throwable.kind) {
                            NetworkException.Kind.CONNECTIVITY -> onNetworkError?.accept(throwable)
                            NetworkException.Kind.DATA -> onDataError?.accept(throwable)
                            else -> return@Consumer
                        }
                    }
                }
    }
}

interface RetryHandler {
    fun retryWithConnectivity(): Function<Flowable<Throwable>, Publisher<Any>>

    class Impl(private val connectivity: Connectivity) : RetryHandler {
        override fun retryWithConnectivity(): Function<Flowable<Throwable>, Publisher<Any>> = Function { errors ->
            val count = AtomicInteger(0)
            errors.flatMap {
                if (it is NetworkException && it.kind === NetworkException.Kind.CONNECTIVITY)
                    Flowable.just(connectivity.internetAvailable())
                else
                    Flowable.timer(Math.min(15000, count.incrementAndGet() * 600).toLong(), TimeUnit.SECONDS)
            }
        }
    }
}

class NetworkException(val throwable: Throwable) : Exception(throwable) {
    enum class Kind {
        CONNECTIVITY,
        DATA,
        UNKNOWN
    }

    val kind: Kind = when (throwable) {
        is UnknownHostException -> Kind.CONNECTIVITY
        is ConnectException -> Kind.CONNECTIVITY
        is HttpException -> Kind.DATA
        else -> Kind.UNKNOWN
    }
}

class Connectivity(private val context: Context) {
    fun internetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
