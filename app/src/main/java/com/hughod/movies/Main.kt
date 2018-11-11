package com.hughod.movies

import android.app.Application
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.hughod.movies.data.dataModule
import com.hughod.movies.ui.ListFragment
import com.hughod.movies.ui.listModule
import com.hughod.movies.util.dataUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.error_view.*
import org.koin.android.ext.android.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(listModule, dataModule, dataUtils))
    }
}

class MainActivity : AppCompatActivity(), ErrorView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        savedInstanceState?.let {
            currentPosition = it.getInt(CURRENT_POSITION, 0)
            return
        }
        ListFragment.launch(supportFragmentManager)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_POSITION, currentPosition)
        super.onSaveInstanceState(outState)
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

    companion object {
        private const val CURRENT_POSITION = "${BuildConfig.APPLICATION_ID}.currentPosition"
        var currentPosition: Int = 0
    }
}

interface ErrorView {
    fun showError()
    fun showError(@StringRes title: Int, @StringRes description: Int)
    fun hideError()
}
