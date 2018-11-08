package com.hughod.movies.list

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hughod.movies.ErrorView
import com.hughod.movies.R
import com.hughod.movies.data.DataProvider
import com.hughod.movies.data.Movies
import com.hughod.movies.detail.DetailFragment
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
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.item_movie.view.*
import kotlinx.android.synthetic.main.list_fragment.*
import org.koin.android.ext.android.inject
import org.koin.dsl.module.module
import java.util.concurrent.TimeUnit

val listModule = module {
    factory { ListPresenter(get(), get(), AndroidSchedulers.mainThread(), Schedulers.io()) }
}

private const val MOVIES = "MOVIES"

//TODO refresh list
class ListFragment : Fragment(), ListPresenter.View {

    override val itemClicked: Observable<Pair<Movie, ImageView>> by lazy { adapter.clicks }
    override val refresh: PublishSubject<Unit> = PublishSubject.create()

    private val presenter: ListPresenter by inject()
    private val adapter = MovieAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.app_name)

        initialiseRecycler()

        presenter.attach(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        recycler?.layoutManager?.onSaveInstanceState()?.let { outState.putParcelable(MOVIES, it) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        presenter.detach(this)
        super.onDestroyView()
    }

    override fun setData(movies: List<Movie>) = adapter.set(movies)

    override fun showMovie(pair: Pair<Movie, ImageView>) =
            DetailFragment.launch(this, pair.first, pair.second)

    override fun showNoNetwork() {
        val activity = activity
        if (activity is ErrorView) activity.showError(R.string.network_error_title, R.string.network_error_description)
    }

    override fun showError() {
        val activity = activity
        if (activity is ErrorView) activity.showError()
    }

    override fun hideError() {
        val activity = activity
        if (activity is ErrorView) activity.hideError()
    }

    private fun initialiseRecycler() {
        recycler?.apply {
            adapter = this@ListFragment.adapter

            layoutManager = GridLayoutManager(context, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int =
                            if (context.resources.configuration.orientation == ORIENTATION_PORTRAIT) 2 else 1
                }
                addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL).apply { setMinimumHeight(5) })
            }
        }
    }

    companion object {
        fun launch(fragmentManager: FragmentManager) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content, ListFragment())
                    .commit()
        }
    }
}

class ListPresenter(
        private val dataProvider: DataProvider,
        private val errorHandler: ErrorHandler,
        private val main: Scheduler,
        private val io: Scheduler
) : Presenter<ListPresenter.View>() {
    override fun attach(view: View) {

        dataProvider.fetchMovies()
                .map { it.toMovieList() }
                .subscribeOn(io)
                .observeOn(main)
                .compose(errorHandler.handle(
                        Consumer { view.showNoNetwork() },
                        Consumer { view.showError() }))
                .subscribeUntilDetached {
                    view.hideError()
                    view.setData(it)
                }

        view.itemClicked
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeUntilDetached { view.showMovie(it) }

        view.refresh
                .throttleFirst(2, TimeUnit.SECONDS)
                .flatMapSingle {
                    dataProvider.fetchMovies()
                            .map { it.toMovieList() }
                            .subscribeOn(io)
                            .observeOn(main)
                }
                .subscribeUntilDetached({ view.setData(it) }, { println(it) })
    }

    interface View : Presenter.View {
        val itemClicked: Observable<Pair<Movie, ImageView>>
        val refresh: PublishSubject<Unit>

        fun setData(movies: List<Movie>)

        fun showMovie(pair: Pair<Movie, ImageView>)
        fun showNoNetwork()
        fun showError()
        fun hideError()
    }
}

class MovieAdapter : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {
    val clicks: PublishSubject<Pair<Movie, ImageView>> = PublishSubject.create()
    private var data = emptyList<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder = MovieViewHolder(parent)

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) = holder.bind(data[position], clicks)

    override fun getItemCount(): Int = data.size

    fun set(data: List<Movie>) {
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
        fun bind(data: Movie, clicks: PublishSubject<Pair<Movie, ImageView>>) {
            itemView.image_view.load(data.imageUrl)
            ViewCompat.setTransitionName(itemView.image_view, data.id.toString())

            itemView.clicks().map { Pair(data, itemView.image_view) }.subscribe(clicks)
        }
    }
}

@Parcelize
data class Movie(val id: Int = 0, val title: String = "", val imageUrl: String = "") : Parcelable

private fun Movies.toMovieList(): List<Movie> = this.movies.map { Movie(it.id, it.title, it.pictureUrl) }
