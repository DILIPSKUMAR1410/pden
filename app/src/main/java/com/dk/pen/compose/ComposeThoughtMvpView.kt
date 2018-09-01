package com.dk.pen.compose

import com.dk.pen.base.MvpView

interface ComposeThoughtMvpView : MvpView {

    fun getThought(): String

    fun setText(text: String?, selection: Int)

    fun showTooManyCharsError()

    fun showEmptyThoughtError()

    fun showSendTweetError()

    fun refreshToolbar()

    fun close()

}