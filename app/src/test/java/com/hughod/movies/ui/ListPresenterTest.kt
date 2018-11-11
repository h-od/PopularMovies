package com.hughod.movies.ui

import com.hughod.movies.data.Api
import com.hughod.movies.data.Movies
import com.hughod.movies.util.ErrorHandler
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class ListPresenterTest {
    private lateinit var presenter: ListPresenter

    private val api = mock<Api>()
    private val errorHandler = mock<ErrorHandler>()
    private val view = mock<ListPresenter.View>()

    private val itemClicks: PublishSubject<Pair<Int, Movies>> = PublishSubject.create()

    @Before
    fun setUp() {
        presenter = ListPresenter(api, errorHandler, Schedulers.trampoline(), Schedulers.trampoline())

        doReturn(itemClicks).whenever(view).itemClicked
    }

    @Test
    fun `when view attached gets movies`() {
        val movies = Movies()

        mockErrorHandler<Movies>()
        doReturn(Single.just(movies)).whenever(api).fetchPopularMovies()

        presenter.attach(view)

        verify(view).setData(eq(movies.list))
    }

    @Test
    fun `when item clicked calls showMovie`() {
        val movies = Movies()

        mockErrorHandler<Movies>()
        doReturn(Single.just(movies)).whenever(api).fetchPopularMovies()

        presenter.attach(view)

        itemClicks.onNext(Pair(0, movies))

        verify(view).setData(eq(movies.list))
    }

    private fun <T> mockErrorHandler() {
        whenever(errorHandler.handle<T>(any(), any())).thenReturn(SingleTransformer { it })
    }
}
