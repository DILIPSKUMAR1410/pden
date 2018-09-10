package com.dk.pen.compose

import android.util.Patterns
import com.dk.pen.base.BasePresenter

class ComposeThoughtPresenter : BasePresenter<ComposeThoughtMvpView>() {

    private val MAX_URL_LENGTH = 23 // it will change
    private var charsLeft: Int = 140
    private var lastAtIndex: Int = -1

    fun charsLeft() = charsLeft

    fun afterTextChanged(text: String) {
        checkLength(text)
    }


    fun onTextChanged(text: String, start: Int, count: Int) {

    }

    private fun sendThought(status: String?) {
        if (status != null)
//            TweetsQueue.add(TweetsQueue.StatusUpdate.valueOf(status))
            mvpView?.close()
    }

    private fun checkLength(text: String) {
        var wordsLength = 0
        var urls = 0

        text.split(" ").forEach { if (isUrl(it)) urls++ else wordsLength += it.length }
        charsLeft = 140 - text.count { it == ' ' } - wordsLength - (urls * MAX_URL_LENGTH)
        mvpView?.refreshToolbar()
    }

    private fun isUrl(text: String) = Patterns.WEB_URL.matcher(text).matches()


}