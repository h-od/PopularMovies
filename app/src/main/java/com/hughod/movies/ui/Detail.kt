package com.hughod.movies.ui

import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hughod.movies.MainActivity
import com.hughod.movies.R
import com.hughod.movies.data.Movies
import com.hughod.movies.util.load
import kotlinx.android.synthetic.main.fragment_detail.view.*

class DetailPagerFragment : Fragment() {

    private val movies: Movies by lazy { arguments?.getParcelable(MOVIES_EXTRA) ?: Movies() }
    private lateinit var viewPager: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewPager = (inflater.inflate(R.layout.fragment_pager, container, false) as ViewPager).apply {
            adapter = MoviePagerAdapter(this@DetailPagerFragment, movies.list)
            currentItem = MainActivity.currentPosition
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    MainActivity.currentPosition = position

                    activity?.title = movies.list[position].title
                }
            })
        }

        if (savedInstanceState == null) postponeEnterTransition()

        return viewPager
    }

    companion object {

        private const val MOVIES_EXTRA = "MOVIES_EXTRA"

        fun create(movies: Movies): DetailPagerFragment = DetailPagerFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(MOVIES_EXTRA, movies)
            arguments = bundle
            exitTransition = Fade()
            enterTransition = Fade()
        }
    }
}

class MoviePagerAdapter(fragment: Fragment, private val data: List<Movies.Movie>) : FragmentStatePagerAdapter(fragment.childFragmentManager) {
    override fun getItem(position: Int): Fragment = DetailFragment.create(data[position])

    override fun getCount(): Int = data.size
}


class DetailFragment : Fragment() {

    private val movie: Movies.Movie by lazy {
        arguments?.getParcelable(MOVIE_EXTRA) ?: Movies.Movie()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        view.subtitle.text = movie.description

        view.description.text = StringBuilder()
                .append(movie.releaseDate).append(" / ").append(movie.duration).append("\n").append("\n")
                .append(movie.synopsis).append("\n").append("\n")
                .append("Starring: ").append("\n").apply {
                    for (actor in movie.actors) this.append(" - ").append(actor).append("\n")
                }
                .toString()

        view.image.load(movie.pictureUrl) {
            parentFragment?.startPostponedEnterTransition()
        }

        return view
    }

    companion object {
        private const val MOVIE_EXTRA = "MOVIE_EXTRA"

        fun create(movie: Movies.Movie) = DetailFragment().apply {
            arguments = Bundle().apply { putParcelable(MOVIE_EXTRA, movie) }
        }
    }
}
