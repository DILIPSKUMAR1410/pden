package com.dk.pden.compose

import android.annotation.SuppressLint
import android.util.Log
import android.util.Patterns
import com.dk.pden.base.BasePresenter
import com.dk.pden.service.ApiServiceFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonArray
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
        dataobj.addProperty("sender", topic)

        // Create a new comment
        val comment = HashMap<String, Any>()
        comment["timestamp"] = rootObject!!.getString("timestamp")
        comment["text"] = rootObject.getString("text")

        val interests = JsonArray()
        interests.add(topic)

        val fcm = JsonObject()
        fcm.add("data",dataobj)

        envelopeObject.add("interests",interests)
        envelopeObject.add("fcm", fcm)

        val admin = HashMap<String, Any>()
        admin["admin"] = topic

        val db = FirebaseFirestore.getInstance()
        db.collection("thoughts").document(rootObject.getString("uuid"))
                .set(admin)
                .addOnSuccessListener { Log.d("ComposeThoughtPresenter", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("ComposeThoughtPresenter", "Error writing document", e) }


        db.collection("thoughts").document(rootObject.getString("uuid")).collection("discussion")
                .add(comment)
                .addOnSuccessListener { Log.d("ComposeThoughtPresenter", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("ComposeThoughtPresenter", "Error writing document", e) }

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