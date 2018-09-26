package com.dk.pden.compose

import com.dk.pden.base.MvpView

interface ComposeThoughtMvpView : MvpView {

    fun getThought(): String

    fun setText(text: String?, selection: Int)

    fun showTooManyCharsError()

    fun showEmptyThoughtError()

    fun showSendTweetError()

    fun refreshToolbar()

    fun close()

    fun showLoading()
}