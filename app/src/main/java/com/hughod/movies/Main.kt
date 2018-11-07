package com.hughod.movies

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.hughod.movies.data.dataModule
import com.hughod.movies.detail.detailModule
import com.hughod.movies.list.listModule
import org.koin.android.ext.android.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(listModule, detailModule, dataModule))
    }
}

class MainActivity: AppCompatActivity() {

}
