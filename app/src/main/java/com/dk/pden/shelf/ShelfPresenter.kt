package com.dk.pden.shelf

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.dk.pden.ObjectBox
import com.dk.pden.base.BasePresenter
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.model.User_
import com.dk.pden.service.ApiServiceFactory
import com.google.gson.JsonObject
import io.objectbox.Box
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

open class ShelfPresenter : BasePresenter<ShelfMvpView>() {

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
        userBox.put(me)
        val envelopeObject = JsonObject()
        val rootObject = JsonObject()
        rootObject.addProperty("timestamp", thought.timestamp)
        rootObject.addProperty("text", thought.text)
        rootObject.addProperty("actual_owner", thought.user.target.blockstackId)
        rootObject.addProperty("uuid", thought.uuid)

        envelopeObject.addProperty("to", "/topics/" + blockstack_id)
        envelopeObject.add("data", rootObject)
        firebaseService.publishToTopic(envelopeObject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = {
                            Log.d("success-->>", "Spreaded")
                        },
                        onError =
                        {
                            Log.d("error-->>", it.message)
                        }
                )
        mvpView?.hideLoading()

    }

}