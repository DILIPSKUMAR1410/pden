package com.dk.pden.discuss

import android.annotation.SuppressLint
import android.util.Log
import com.dk.pden.base.BasePresenter
import com.dk.pden.base.MvpView
import com.dk.pden.service.ApiResponses.PublishStatusApiResponse
import com.dk.pden.service.ApiServiceFactory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


class DiscussPresenter : BasePresenter<MvpView>() {

    private val firebaseService by lazy {
        ApiServiceFactory.createFirebaseService()
    }


    @SuppressLint("CheckResult")
    fun sendComment(admin: String, topic: String, comment: HashMap<String, Any>) {
        val envelopeObject = JsonObject()
        val dataobj = JsonObject()
        dataobj.addProperty("timestamp", comment["timestamp"].toString())
        dataobj.addProperty("uuid", comment["uuid"].toString())
        dataobj.addProperty("text", comment["text"].toString())
        dataobj.addProperty("topicId", topic)
        dataobj.addProperty("isComment", true)
        dataobj.addProperty("actual_owner", comment["actual_owner"].toString())
        val to = JsonArray()
        val via: Single<PublishStatusApiResponse>
        if (admin.equals(comment["actual_owner"].toString())) {
            to.add(topic)
            envelopeObject.add("interests", to)
            via = firebaseService.publishToTopic(envelopeObject)
        } else {
            to.add(admin)
            envelopeObject.add("users", to)
            via = firebaseService.publishToUser(envelopeObject)
        }

        val fcm = JsonObject()
        fcm.add("data", dataobj)

        envelopeObject.add("fcm", fcm)

        via.observeOn(AndroidSchedulers.mainThread())
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

    }
}