package com.hughod.movies.data

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import org.koin.dsl.module.module
import retrofit2.http.GET

val dataModule = module {

}

class DataManager {
    fun fetchMovie(id: Int): Single<Movies> {
        return Single.never()
    }

    fun fetchMovies(): Single<Movies.Movie> {
        return Single.never()
    }
}

interface Api {
    @GET("tmgmobilepub/articles.json")
    fun fetchPopularMovies(): Single<Movies>
}

data class Movies(
        @SerializedName("collection") val movies: List<Movie> = listOf()
) {
    data class Movie(
            @SerializedName("id") val id: Int = 0,
            @SerializedName("website-url") val websiteUrl: String = "",
            @SerializedName("headline") val headline: String = "",
            @SerializedName("description") val description: String = "",
            @SerializedName("article-body") val articleBody: String = "",
            @SerializedName("ratings") val ratings: Int = 0,
            @SerializedName("picture-url") val pictureUrl: String = "",
            @SerializedName("video-url") val videoUrl: String = "",
            @SerializedName("actors") val actors: List<String> = listOf(),
            @SerializedName("director") val director: String = "",
            @SerializedName("genre") val genre: List<String> = listOf(),
            @SerializedName("synopsis") val synopsis: String = "",
            @SerializedName("release-date") val releaseDate: String = "",
            @SerializedName("duration") val duration: String = "",
            @SerializedName("published-date") val publishedDate: String = "",
            @SerializedName("author") val author: Author = Author()
    ) {
        data class Author(
                @SerializedName("name") val name: String = "",
                @SerializedName("headshot") val headShot: String = "",
                @SerializedName("twitter") val twitter: String = ""
        )
    }
}
