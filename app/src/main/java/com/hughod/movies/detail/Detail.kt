package com.hughod.movies.detail

import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hughod.movies.ErrorView
import com.hughod.movies.R
import com.hughod.movies.data.DataProvider
import com.hughod.movies.data.Movies
import com.hughod.movies.list.Movie
import com.hughod.movies.util.ErrorHandler
import com.hughod.movies.util.Presenter
import com.hughod.movies.util.addImageTransition
import com.hughod.movies.util.load
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.detail_fragment.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module.module
import java.util.concurrent.TimeUnit

val detailModule = module {
    factory { (id: Int) -> DetailPresenter(id, get(), get(), AndroidSchedulers.mainThread(), Schedulers.io()) }
}

//TODO dialog with author & link to twitter
//TODO view pager for next and previous
//TODO save instance state
class DetailFragment : Fragment(), DetailPresenter.View {

    private val movie: Movie by lazy { arguments?.getParcelable(MOVIE_EXTRA) ?: Movie() }
    private val presenter: DetailPresenter by inject { parametersOf(movie.id) }

    override val authorClicked: Observable<Detail.Author> = Observable.empty() //TODO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.detail_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
    }

    override fun onDestroyView() {
        presenter.detach(this)
        super.onDestroyView()
    }

    override fun setData(movie: Detail) {
        activity?.title = movie.title
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)

        image.load(movie.pictureUrl)

        movie_title.text = movie.title
        description.text = movie.description
    }

    override fun showNoNetwork() {
        val activity = activity
        if (activity is ErrorView) activity.showError(R.string.network_error_title, R.string.network_error_description)
    }

    override fun showError() {
        val activity = activity
        if (activity is ErrorView) activity.hideError()
    }

    override fun showAuthor(author: Detail.Author) = TODO()

    companion object {
        private const val MOVIE_EXTRA = "MOVIE_EXTRA"

        fun launch(current: Fragment, movie: Movie, image: ImageView?) {
            val fragmentManager = current.fragmentManager ?: return
            val destination = DetailFragment()

            setFragmentTransitions(current, destination)

            destination.arguments = Bundle().apply { putParcelable(MOVIE_EXTRA, movie) }

            val fragmentTransaction = fragmentManager.beginTransaction()

            image?.let {
                fragmentTransaction.addImageTransition(
                        destination, it, it.context.resources.getString(R.string.transition_name))
            }

            fragmentTransaction
                    .replace(R.id.content, destination)
                    .addToBackStack(null)
                    .commit()
        }

        private fun setFragmentTransitions(vararg fragments: Fragment) {

            val fade = Fade()

            for (fragment in fragments) {
                fragment.enterTransition = fade
                fragment.exitTransition = fade
                fragment.returnTransition = fade
                fragment.reenterTransition = fade
            }
        }
    }
}

class DetailPresenter(
        private val id: Int,
        private val dataProvider: DataProvider,
        private val errorHandler: ErrorHandler,
        private val main: Scheduler,
        private val io: Scheduler
) : Presenter<DetailPresenter.View>() {
    override fun attach(view: View) {
        dataProvider.fetchMovie(id)
                .observeOn(main)
                .subscribeOn(io)
                .compose(errorHandler.handle(
                        Consumer { view.showNoNetwork() },
                        Consumer { view.showError() }))
                .map { it.toMovieDetail() }
                .subscribeUntilDetached { view.setData(it) }

        view.authorClicked
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeUntilDetached { view.showAuthor(it) }
    }

    interface View : Presenter.View {
        val authorClicked: Observable<Detail.Author>

        fun setData(movie: Detail)

        fun showNoNetwork()
        fun showError()
        fun showAuthor(author: Detail.Author)
    }
}

data class Detail(
        val id: Int = 0,
        val title: String = "",
        val pictureUrl: String = "",
        val description: String = "",
        val synopsis: String = "",
        val ratings: Int = 0,
        val actors: List<String> = listOf(),
        val director: String = "",
        val releaseDate: String = "",
        val duration: String = "",
        val publishedDate: String = "",
        val author: Author = Author()) {
    data class Author(
            val name: String = "",
            val headShot: String = "",
            val twitter: String = "")
}

private fun Movies.Movie.toMovieDetail(): Detail = Detail(
        id, title, pictureUrl, description, synopsis, ratings, actors, director, releaseDate, duration, publishedDate)
