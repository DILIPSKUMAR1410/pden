package com.dk.pden.compose

import android.annotation.SuppressLint
import android.util.Log
import android.util.Patterns
import com.dk.pden.base.BasePresenter
import com.dk.pden.service.ApiServiceFactory
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject


class ComposeThoughtPresenter : BasePresenter<ComposeThoughtMvpView>() {

    private val MAX_URL_LENGTH = 23 // it will change
    private var charsLeft: Int = 256
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

    @SuppressLint("CheckResult")
    fun sendThought(topic: String, rootObject: JSONObject?) {
        val envelopeObject = JsonObject()
        val dataobj = JsonObject()
        dataobj.addProperty("timestamp", rootObject?.getString("timestamp"))
        dataobj.addProperty("uuid", rootObject?.getString("uuid"))
        dataobj.addProperty("text", rootObject?.getString("text"))
        if (rootObject?.has("actual_owner")!!) dataobj.addProperty("actual_owner", rootObject.getString("actual_owner"))

        envelopeObject.addProperty("to", "/topics/$topic")
        envelopeObject.addProperty("priority", "high")
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
        charsLeft = 256 - text.count { it == ' ' } - wordsLength - (urls * MAX_URL_LENGTH)
        mvpView?.refreshToolbar()
    }

    private fun isUrl(text: String) = Patterns.WEB_URL.matcher(text).matches()


}