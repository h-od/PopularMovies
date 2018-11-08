package com.hughod.movies.util

import androidx.annotation.CallSuper
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Contract for interaction between view and presenter (MVP)
 */
abstract class Presenter<in V : Presenter.View> {

    private val disposables by lazy { CompositeDisposable() }

    abstract fun attach(view: V)

    @CallSuper
    open fun detach(view: V) {
        this.disposables.clear()
    }

    private fun clearOnDetached(disposable: Disposable) {
        this.disposables.add(disposable)
    }

    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit): Disposable =
            subscribe(onNext).apply { clearOnDetached(this) }

    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
            subscribe(onNext, onError).apply { clearOnDetached(this) }

    protected fun <T> Single<T>.subscribeUntilDetached(onNext: (T) -> Unit): Disposable =
            subscribe(onNext).apply { clearOnDetached(this) }

    protected fun <T> Single<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
            subscribe(onNext, onError).apply { clearOnDetached(this) }

    interface View
}
