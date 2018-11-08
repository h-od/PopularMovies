package com.hughod.movies

import android.app.Application
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.hughod.movies.data.dataModule
import com.hughod.movies.detail.detailModule
import com.hughod.movies.list.ListFragment
import com.hughod.movies.list.listModule
import com.hughod.movies.util.dataUtils
import kotlinx.android.synthetic.main.error_view.*
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.android.ext.android.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(listModule, detailModule, dataModule, dataUtils))
    }
}

class MainActivity : AppCompatActivity(), ErrorView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        ListFragment.launch(supportFragmentManager)
    }

    override fun showError() {
        showError(R.string.generic_error_title, R.string.generic_error_description)
    }

    override fun showError(@StringRes title: Int, @StringRes description: Int) {
        error_title.setText(title)
        error_description.setText(description)
        error_view.visibility = VISIBLE
    }

    override fun hideError() {
        error_view.visibility = GONE
    }
}

interface ErrorView {
    fun showError()
    fun showError(@StringRes title: Int, @StringRes description: Int)
    fun hideError()
}
