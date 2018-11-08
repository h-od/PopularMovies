package com.hughod.movies.util

import android.graphics.drawable.Drawable
import android.transition.ChangeBounds
import android.transition.TransitionSet
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

fun ImageView?.load(url: String, function: (() -> Unit)? = null) {
    this ?: return

    val progressDrawable = CircularProgressDrawable(context)
            .apply {
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

    val requestOptions = RequestOptions()
            .placeholder(progressDrawable)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .fitCenter()

    Glide.with(this)
            .setDefaultRequestOptions(requestOptions)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    function?.invoke()
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    function?.invoke()
                    return false
                }
            })
            .into(this)
}

fun FragmentTransaction.addImageTransition(destFragment: Fragment, sourceView: View?, transitionName: String): FragmentTransaction {

    sourceView ?: return this

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

        val transition = TransitionSet().addTransition(ChangeBounds())

        destFragment.sharedElementEnterTransition = transition
        destFragment.sharedElementReturnTransition = transition
    }

    return this.addSharedElement(sourceView, transitionName)
}
