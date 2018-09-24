package com.dk.pen.compose

import android.util.Log
import android.util.Patterns
import com.dk.pen.base.BasePresenter
import com.dk.pen.service.ApiServiceFactory
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject


class ComposeThoughtPresenter : BasePresenter<ComposeThoughtMvpView>() {

    private val MAX_URL_LENGTH = 23 // it will change
    private var charsLeft: Int = 140
    private var lastAtIndex: Int = -1
    private val firebaseService by lazy {
        ApiServiceFactory.createFirebaseService()
    }

    fun charsLeft() = charsLeft

    fun afterTextChanged(text: String) {
        checkLength(text)
    }


    fun onTextChanged(text: String, start: Int, count: Int) {

    }

    fun sendThought(blockstack_id: String, rootObject: JSONObject?) {
        val envelopeObject = JsonObject()
        val dataobj = JsonObject()
        dataobj.addProperty("timestamp", rootObject?.getString("timestamp"))
        dataobj.addProperty("text", rootObject?.getString("text"))
        envelopeObject.addProperty("to", "/topics/" + blockstack_id)
        envelopeObject.add("data", dataobj)
        firebaseService.publishToTopic(envelopeObject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = {
                            Log.d("success-->>", "Shared")
                        },
                        onError =
                        {
                            Log.d("error-->>", it.message)
                        }
                )
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