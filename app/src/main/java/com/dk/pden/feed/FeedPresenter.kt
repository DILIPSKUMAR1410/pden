package com.dk.pden.feed

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.ObjectBox
import com.dk.pden.base.BasePresenter
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.model.User_
import com.dk.pden.service.ApiServiceFactory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.objectbox.Box
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

open class FeedPresenter : BasePresenter<FeedMvpView>() {

    private lateinit var userBox: Box<User>
    private lateinit var thoughtBox: Box<Thought>
    private val firebaseService by lazy {
        ApiServiceFactory.createFirebaseService()
    }

    @SuppressLint("CheckResult")
    fun spreadThought(thought: Thought, context: Context) {

        mvpView?.showLoading()

        val blockstack_id = PreferencesHelper(context).blockstackId
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        val me = userBox.find(User_.blockstackId, blockstack_id).first()
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        thought.isSpread = true
        thoughtBox.put(thought)
        mvpView?.updateAdapter()
        userBox.put(me)
        val envelopeObject = JsonObject()
        val rootObject = JsonObject()
        val props = JSONObject()

        rootObject.addProperty("timestamp", thought.timestamp)
        rootObject.addProperty("text", thought.textString)
        rootObject.addProperty("actual_owner", thought.user.target.blockstackId)
        rootObject.addProperty("uuid", thought.uuid)
        rootObject.addProperty("from", blockstack_id)

        val interests = JsonArray()
        interests.add(blockstack_id)
        val fcm = JsonObject()
        fcm.add("data", rootObject)

        envelopeObject.add("interests", interests)
        envelopeObject.add("fcm", fcm)

        firebaseService.publishToTopic(envelopeObject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = {
                            Log.d("success-->>", "Spread")
                            props.put("Success", true)
                            mixpanel.track("Thought spread", props)
                            mixpanel.people.increment("Spread", 1.0)
                        },
                        onError =
                        {
                            Log.d("error-->>", it.message)
                            props.put("Success", false)
                            mixpanel.track("Thought spread", props)
                        }
                )
        mvpView?.hideLoading()

    }

}