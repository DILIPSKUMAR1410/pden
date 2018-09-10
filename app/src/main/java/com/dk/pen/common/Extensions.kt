package com.dk.pen.common

import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions






/**
 * Created by andrea on 27/09/16.
 */
fun View.visible(show: Boolean = true) {
    this.visibility = if (show) View.VISIBLE else View.GONE
}

fun ImageView.loadAvatar(url: CharSequence?) {
    // TODO placeholder
    Log.d("url-->>", url.toString())
    val options = RequestOptions()
    options.fitCenter()
    Glide.with(context).load(url).apply(options).into(this)
}

