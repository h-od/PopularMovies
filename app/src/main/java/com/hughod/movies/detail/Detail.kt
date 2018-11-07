package com.hughod.movies.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hughod.movies.util.Presenter
import org.koin.android.ext.android.inject
import org.koin.dsl.module.module

val detailModule = module {
    single { DetailPresenter() }
}

class DetailFragment: Fragment(), DetailPresenter.View {
    private val presenter: Presenter<DetailPresenter.View> by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
    }

    override fun onDestroyView() {
        presenter.detach(this)
        super.onDestroyView()
    }
}

class DetailPresenter: Presenter<DetailPresenter.View>() {
    override fun attach(view: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    interface View: Presenter.View {

    }
}
