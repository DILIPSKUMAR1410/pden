package com.dk.pen.mybook

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dk.pen.ObjectBox
import com.dk.pen.base.BasePresenter
import com.dk.pen.model.Thought
import com.dk.pen.model.Thought_
import com.dk.pen.model.User
import com.dk.pen.model.User_
import io.objectbox.Box
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.json.JSONArray


open class MyBookPresenter : BasePresenter<MyBookMvpView>() {
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var userBox: Box<User>
    val config = java.net.URI("https://condescending-fermat-e43740.netlify.com").run {
        org.blockstack.android.sdk.BlockstackConfig(
                this,
                java.net.URI("${this}/redirect/"),
                java.net.URI("${this}/manifest.json"),
                kotlin.arrayOf(org.blockstack.android.sdk.Scope.StoreWrite))
    }
//    var page: Int = 1
//    protected var isLoading: Boolean = false

//    open fun getThoughts() {
//        checkViewAttached()
//        mvpView?.showLoading()
//        isLoading = true


//        disposables
//                .add(TwitterAPI.getHomeTimeline(Paging(page, 50))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    mvpView?.hideLoading()
//
//                    when {
//                        it == null -> mvpView?.showError()
//                        it.isEmpty() -> mvpView?.showEmpty()
//                        else -> {
//                            mvpView?.showThoughts(it.map(::Tweet).toMutableList())
//                            page++
//                        }
//                    }
//
//                    isLoading = false
//                }, {
//                    Timber.e(it?.message)
//                    mvpView?.hideLoading()
//                    mvpView?.showError()
//                    isLoading = false
//                }))
//    }

//    open fun getMoreThoughts(context: Context) {
//        if (isLoading)
//            return
//
//        checkViewAttached()
//        isLoading = true

//        disposables.add(TwitterAPI.getHomeTimeline(Paging(page, 50))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    if (it != null) {
//                        if (it.isNotEmpty())
//                            mvpView?.showMoreTweets(it.map(::Tweet).toMutableList())
//                        page++
//                    }
//                    isLoading = false
//                }, {
//                    Timber.e(it?.message)
//                    isLoading = false
//                }))
//    }

    open fun onRefresh(context: Context, blockstack_id: String) {

        checkViewAttached()
//        val sinceId = mvpView?.getLastMyThoughtId()
//        if (sinceId != null && sinceId > 0) {
//            val page = Paging(1, 200)
//            page.sinceId = sinceId
//            disposables.add(TwitterAPI.refreshTimeLine(page)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({
//                        mvpView?.stopRefresh()
//                        if (it != null) {
//                            it.reversed().forEach { status -> mvpView?.showTweet(Tweet(status)) }
//                        } else {
//                            mvpView?.showSnackBar(R.string.error_refreshing_timeline)
//                        }
//                    }, {
//                        Timber.e(it?.message)
//                        mvpView?.stopRefresh()
//                        mvpView?.showSnackBar(R.string.error_refreshing_timeline)
//                    }))
//        } else mvpView?.stopRefresh()
        Log.d(">>>>>>>>", "Working")
        _blockstackSession = BlockstackSession(context, config,
                onLoadedCallback = {
                    // Wait until this callback fires before using any of the
                    // BlockstackSession API methods
                    val options = GetFileOptions()
                    blockstackSession().getFile("MyThoughts.json", options, { contentResult ->
                        if (contentResult.hasValue) {
                            val content = contentResult.value!!.toString()
                            var thoughts = mutableListOf<Thought>()
                            for (i in 0..(JSONArray(content).length() - 1)) {
                                val item = JSONArray(content).getJSONObject(i)

                                // Your code here
                                val thought = Thought(item.getString("text"), item.getLong("timestamp"))
                                Log.d("thought", thought.toString())
                                thoughts.add(thought)

                            }
                            thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
                            userBox = ObjectBox.boxStore.boxFor(User::class.java)
                            val user = userBox.find(User_.blockstackId, blockstack_id).first()
                            thoughtBox.query().run {
                                equal(Thought_.userId, user.id)
                                build().remove()
                            }
                            user.thoughts.addAll(thoughts)
                            userBox.put(user)
                            mvpView?.stopRefresh()
                            mvpView?.showThoughts(thoughts)


                        } else {
                            Toast.makeText(context, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                        }
                    })
                })


//        }
    }


    fun blockstackSession(): BlockstackSession {
        val session = _blockstackSession
        if (session != null) {
            return session
        } else {
            throw IllegalStateException("No session.")
        }
    }
}