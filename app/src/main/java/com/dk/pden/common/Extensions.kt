package com.dk.pden.common

import android.annotation.SuppressLint
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

@SuppressLint("CheckResult")
fun ImageView.loadAvatar(url: CharSequence?) {
    // TODO placeholder
    val options = RequestOptions()
    options.fitCenter()
    Glide.with(context).load(url).apply(options).into(this)
}

