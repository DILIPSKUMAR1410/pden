package com.dk.pen.mybook

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.dk.pen.ObjectBox
import com.dk.pen.base.BasePresenter
import com.dk.pen.common.Utils.config
import com.dk.pen.model.Thought
import com.dk.pen.model.Thought_
import com.dk.pen.model.User
import com.dk.pen.model.User_
import io.objectbox.Box
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.Result
import org.json.JSONArray
import java.net.URL


open class MyBookPresenter : BasePresenter<MyBookMvpView>() {
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var userBox: Box<User>

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

    open fun onRefresh(context: Activity, blockstack_id: String, self: Boolean) {

        checkViewAttached()
        var options = GetFileOptions(false)
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


        _blockstackSession = BlockstackSession(context, config
        ) {
            // Wait until this callback fires before using any of the
            // BlockstackSession API methods
            if (self) {
                blockstackSession().getFile("book.json", options) { contentResult ->
                    if (contentResult.hasValue) {
                        var my_book = JSONArray()
                        val content: Any
                        if (contentResult.value is String) {
                            content = contentResult.value as String
                            if (content.isNotEmpty()) {
                                my_book = JSONArray(content)
                            }
                        }
                        var thoughts = mutableListOf<Thought>()
                        for (i in 0..(my_book.length() - 1)) {
                            val item = my_book.getJSONObject(i)

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
                }
            } else {
                val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
                options = GetFileOptions(username = blockstack_id,
                        zoneFileLookupURL = zoneFileLookupUrl,
                        app = "https://condescending-fermat-e43740.netlify.com",
                        decrypt = false)
                blockstackSession().lookupProfile(blockstack_id, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
                    if (profileResult.hasValue) {
                        launch(UI) {
                            blockstackSession().getFile("book.json", options) { contentResult: Result<Any> ->
                                if (contentResult.hasValue) {
                                    var my_book = JSONArray()
                                    Log.d("thoughts", my_book.toString())
                                    val content: Any
                                    if (contentResult.value is String) {
                                        content = contentResult.value as String
                                        if (content.isNotEmpty()) {
                                            my_book = JSONArray(content)
                                        }
                                    }
                                    var thoughts = mutableListOf<Thought>()
                                    for (i in 0..(my_book.length() - 1)) {
                                        val item = my_book.getJSONObject(i)

                                        // Your code here
                                        val thought = Thought(item.getString("text"), item.getLong("timestamp"))
                                        thoughts.add(thought)

                                    }
                                    //                thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
                                    //                userBox = ObjectBox.boxStore.boxFor(User::class.java)
                                    //                val user = userBox.find(User_.blockstackId, blockstack_id).first()
                                    //                thoughtBox.query().run {
                                    //                    equal(Thought_.userId, user.id)
                                    //                    build().remove()
                                    //                }
                                    //                user.thoughts.addAll(thoughts)
                                    //                userBox.put(user)
                                    mvpView?.stopRefresh()
                                    mvpView?.showThoughts(thoughts)
                                } else {
                                    val errorMsg = "error: " + contentResult.error
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    } else {
                        val errorMsg = "error: " + profileResult.error
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }


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