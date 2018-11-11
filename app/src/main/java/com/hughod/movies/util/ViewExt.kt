package com.hughod.movies.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

fun ImageView?.load(url: String, function: ((ImageView) -> Unit)? = null) {
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
                    function?.let { it(this@load) }
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    function?.let { it(this@load) }
                    return false
                }
            })
            .into(this)
}
