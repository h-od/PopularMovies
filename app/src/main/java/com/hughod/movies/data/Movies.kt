package com.hughod.movies.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hughod.movies.BuildConfig.BASE_URL
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

val dataModule = module {
    single { get<Retrofit>().create<Api>(Api::class.java) }

    single {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                        OkHttpClient.Builder()
                                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                                .cache(Cache(androidApplication().cacheDir, 10 * 1024 * 1024))//10mb
                                .build())
                .build()
    }
}

interface Api {
    @GET("tmgmobilepub/articles.json")
    fun fetchPopularMovies(): Single<Movies>
}

@Parcelize
data class Movies(
        @SerializedName("collection") val list: List<Movie> = listOf()
): Parcelable {
    @Parcelize
    data class Movie(
            @SerializedName("id") val id: Int = 0,
            @SerializedName("headline") val title: String = "",
            @SerializedName("website-url") val websiteUrl: String = "",
            @SerializedName("picture-url") val pictureUrl: String = "",
            @SerializedName("description") val description: String = "",
            @SerializedName("synopsis") val synopsis: String = "",
            @SerializedName("ratings") val ratings: Int = 0,
            @SerializedName("actors") val actors: List<String> = listOf(),
            @SerializedName("director") val director: String = "",
            @SerializedName("release-date") val releaseDate: String = "",
            @SerializedName("duration") val duration: String = "",
            @SerializedName("published-date") val publishedDate: String = "",
            @SerializedName("author") val author: Author = Author(),
            val transitionName: String = "image_$id") : Parcelable {

        @Parcelize
        data class Author(
                @SerializedName("name") val name: String = "",
                @SerializedName("headshot") val headShot: String = "",
                @SerializedName("twitter") val twitter: String = ""
        ) : Parcelable
    }
}
