package com.hughod.movies.ui

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hughod.movies.ErrorView
import com.hughod.movies.MainActivity
import com.hughod.movies.R
import com.hughod.movies.data.Api
import com.hughod.movies.data.Movies
import com.hughod.movies.util.ErrorHandler
import com.hughod.movies.util.Presenter
import com.hughod.movies.util.load
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_movie.view.*
import org.koin.android.ext.android.inject
import org.koin.dsl.module.module
import java.util.concurrent.TimeUnit

val listModule = module {
    factory { ListPresenter(get(), get(), AndroidSchedulers.mainThread(), Schedulers.io()) }
}

class ListFragment : Fragment(), ListPresenter.View {

    override val itemClicked: Observable<Pair<Int, Movies>> by lazy { adapter.clicks }

    private val presenter: ListPresenter by inject()

    private val adapter by lazy { MoviesAdapter() }

    private lateinit var recycler: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recycler = inflater.inflate(R.layout.fragment_list, container, false) as RecyclerView

        activity?.title = getString(R.string.app_name)

        recycler.apply {
            adapter = this@ListFragment.adapter

            layoutManager = GridLayoutManager(context, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int =
                            if (context.resources.configuration.orientation == ORIENTATION_PORTRAIT) 2 else 1
                }
            }
        }

        return recycler
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
        scrollToPosition()
        exitTransition = Fade()
        enterTransition = Fade()
    }

    override fun onDestroyView() {
        presenter.detach(this)
        super.onDestroyView()
    }

    override fun setData(movies: List<Movies.Movie>) = adapter.set(movies)

    override fun showMovie(pair: Pair<Int, Movies>) {
        MainActivity.currentPosition = pair.first
        val movies = pair.second

        activity?.title = movies.list[pair.first].title

        fragmentManager?.apply {
            beginTransaction()
                    .replace(R.id.content, DetailPagerFragment.create(movies), DetailPagerFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun showNoNetwork() {
        (activity as? ErrorView)?.showError(R.string.network_error_title, R.string.network_error_description)
    }

    override fun showError() {
        (activity as? ErrorView)?.showError()
    }

    override fun hideError() {
        (activity as? ErrorView)?.hideError()
    }

    private fun scrollToPosition() {
        recycler.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                    v: View?, left: Int, top: Int, right: Int, bottom: Int,
                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                recycler.removeOnLayoutChangeListener(this)
                val layoutManager = recycler.layoutManager
                val viewAtPosition = layoutManager?.findViewByPosition(MainActivity.currentPosition)

                if (viewAtPosition == null
                        || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true))
                    recycler.post { layoutManager?.scrollToPosition(MainActivity.currentPosition) }
            }
        })
    }

    companion object {
        fun launch(fragmentManager: FragmentManager) =
                fragmentManager.beginTransaction()
                        .replace(R.id.content, ListFragment(), ListFragment::class.java.simpleName)
                        .commit()
    }
}

class ListPresenter(
        private val api: Api,
        private val errorHandler: ErrorHandler,
        private val main: Scheduler,
        private val io: Scheduler
) : Presenter<ListPresenter.View>() {

    override fun attach(view: View) {
        api.fetchPopularMovies()
                .subscribeOn(io)
                .observeOn(main)
                .compose(errorHandler.handle(
                        Consumer { view.showNoNetwork() },
                        Consumer { view.showError() }))
                .subscribeUntilDetached {
                    view.hideError()
                    view.setData(it.list)
                }

        view.itemClicked
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeUntilDetached { view.showMovie(it) }
    }

    interface View : Presenter.View {
        val itemClicked: Observable<Pair<Int, Movies>>

        fun setData(movies: List<Movies.Movie>)

        fun showMovie(pair: Pair<Int, Movies>)
        fun showNoNetwork()
        fun showError()
        fun hideError()
    }
}

class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {
    val clicks: PublishSubject<Pair<Int, Movies>> = PublishSubject.create()
    private var data = emptyList<Movies.Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder = MovieViewHolder(parent)

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) = holder.bind(position, data, clicks)

    override fun getItemCount(): Int = data.size

    fun set(data: List<Movies.Movie>) {
        if (this.data == data) return
        this.data = data
        this.notifyDataSetChanged()
    }

    class MovieViewHolder(
            parent: ViewGroup
    ) : RecyclerView.ViewHolder(
            LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_movie, parent, false)
    ) {
        fun bind(position: Int, movies: List<Movies.Movie>, clicks: PublishSubject<Pair<Int, Movies>>) {
            itemView.image_view.load(movies[position].pictureUrl)
            itemView.clicks().map { Pair(position, Movies(movies)) }.subscribe(clicks)
        }
    }
}
