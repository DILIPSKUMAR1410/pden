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
import com.google.firebase.messaging.FirebaseMessaging
import io.objectbox.Box
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import org.blockstack.android.sdk.Result
import org.json.JSONArray
import java.net.URL


open class MyBookPresenter : BasePresenter<MyBookMvpView>() {
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var userBox: Box<User>

    open fun onRefresh(context: Activity, user: User, self: Boolean) {
        checkViewAttached()
        var thoughts = mutableListOf<Thought>()
        var options = GetFileOptions(false)
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        userBox = ObjectBox.boxStore.boxFor(User::class.java)

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
                        var i = 0
                        while (i < my_book.length()) {
                            val item = my_book.getJSONObject(i)
                            // Your code here
                            val thought = Thought(item.getString("text"), item.getLong("timestamp"))
                            thoughts.add(thought)
                            i++
                        }
                        thoughtBox.query().run {
                            equal(Thought_.userId, user.id)
                            build().remove()
                        }
                        user.thoughts.addAll(thoughts)
                        userBox.put(user)
                        mvpView?.showThoughts(thoughts)
                        mvpView?.stopRefresh()
                    } else {
                        Toast.makeText(context, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
                options = GetFileOptions(username = user.blockstackId,
                        zoneFileLookupURL = zoneFileLookupUrl,
                        app = "https://condescending-fermat-e43740.netlify.com",
                        decrypt = false)
                thoughts = mutableListOf<Thought>()
                blockstackSession().lookupProfile(user.blockstackId, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
                    if (profileResult.hasValue) {
                        launch(UI) {
                            blockstackSession().getFile("book.json", options) { contentResult: Result<Any> ->
                                if (contentResult.hasValue) {
                                    var my_book = JSONArray()
                                    val content: Any
                                    if (contentResult.value is String) {
                                        content = contentResult.value as String
                                        if (content.isNotEmpty()) {
                                            my_book = JSONArray(content)
                                        }
                                    }
                                    var i = 0
                                    while (i < my_book.length()) {
                                        val item = my_book.getJSONObject(i)
                                        // Your code here
                                        val thought = Thought(item.getString("text"), item.getLong("timestamp"))
                                        thoughts.add(thought)
                                        i++
                                    }
                                    Log.d("thoughts -> Mybook open", my_book.toString())
                                    if (!userBox.find(User_.blockstackId, user.blockstackId).isEmpty()) {
                                        thoughtBox.query().run {
                                            equal(Thought_.userId, user.id)
                                            build().remove()
                                        }
                                        user.thoughts.addAll(thoughts)
                                        userBox.put(user)
                                    }
                                    mvpView?.showThoughts(thoughts)
                                    mvpView?.stopRefresh()
                                } else {
                                    val errorMsg = "error: " + contentResult.error
                                    Log.d("errorMsg", errorMsg)

                                }

                            }
                        }
                    } else {
                        val errorMsg = "error: " + profileResult.error
                        Log.d("errorMsg", errorMsg)

                    }
                }
            }


        }

    }

    fun addInterest(context: Activity, user: User) {
        mvpView?.showLoading()
        var interests = JSONArray()
        val options_get = GetFileOptions(false)

        _blockstackSession = BlockstackSession(context, config
        ) {
            blockstackSession().getFile("interest_page_0.json", options_get) { contentResult ->
                launch(UI) {
                    if (contentResult.hasValue) {
                        var content: String? = null
                        if (contentResult.value is String) {
                            content = contentResult.value as String
                            if (content.isNotEmpty()) interests = JSONArray(content)

                            if (!content.contains(user.blockstackId)) {
                                interests.put(user.blockstackId)
                                Log.d("Final content", interests.toString())
                                val options_put = PutFileOptions(false)

                                blockstackSession().putFile("interest_page_0.json", interests.toString(), options_put)
                                { readURLResult ->
                                    if (readURLResult.hasValue) {
                                        val readURL = readURLResult.value!!
                                        val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
                                        var options = GetFileOptions(username = user.blockstackId,
                                                zoneFileLookupURL = zoneFileLookupUrl,
                                                app = "https://condescending-fermat-e43740.netlify.com",
                                                decrypt = false)
                                        var thoughts = mutableListOf<Thought>()
                                        launch(UI) {
                                            blockstackSession().lookupProfile(user.blockstackId, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
                                                if (profileResult.hasValue) {
                                                    launch(UI) {
                                                        blockstackSession().getFile("book.json", options) { contentResult: Result<Any> ->
                                                            if (contentResult.hasValue) {
                                                                var my_book = JSONArray()
                                                                if (contentResult.value is String) {
                                                                    content = contentResult.value as String
                                                                    if (content!!.isNotEmpty()) {
                                                                        my_book = JSONArray(content)
                                                                    }
                                                                }
                                                                var i = 0
                                                                while (i < my_book.length()) {
                                                                    val item = my_book.getJSONObject(i)
                                                                    // Your code here
                                                                    val thought = Thought(item.getString("text"), item.getLong("timestamp"))
                                                                    thoughts.add(thought)
                                                                    i++
                                                                }
                                                                Log.d("thoughts -> adding", my_book.toString())
                                                                userBox = ObjectBox.boxStore.boxFor(User::class.java)
                                                                user.thoughts.addAll(thoughts)
                                                                userBox.put(user)
                                                                Log.d("user id", user.id.toString())
                                                                Log.d("MyFirebaseMsgService", "Subscribing to news topic")
                                                                // [START subscribe_topics]
                                                                FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + user.blockstackId)
                                                                        .addOnCompleteListener { task ->
                                                                            var msg = "Added"
                                                                            if (!task.isSuccessful) {
                                                                                msg = "Error in firebase"
                                                                            }
                                                                            Log.d("MyFirebaseMsgService", msg)
                                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                                        }
                                                                // [END subscribe_topics]

                                                            } else {
                                                                val errorMsg = "error: " + contentResult.error
                                                                Log.d("errorMsg", errorMsg)
                                                            }

                                                        }
                                                    }

                                                } else {
                                                    val errorMsg = "error: " + profileResult.error
                                                    Log.d("errorMsg", errorMsg)
                                                }
                                            }
                                        }

                                        Log.d("Gaia URL", "File stored at: ${readURL}")
                                    } else {
                                        Toast.makeText(context, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
                                    }
                                }

                            } else
                                Log.d("Already added", interests.toString())
                            mvpView?.hideLoading()
                        }
                    } else {
                        Toast.makeText(context, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    fun removeInterest(context: Activity, user: User) {
        mvpView?.showLoading()
        var interests = JSONArray()
        val options_get = GetFileOptions(false)

        _blockstackSession = BlockstackSession(context, config
        ) {
            blockstackSession().getFile("interest_page_0.json", options_get) { contentResult ->
                launch(UI) {
                    if (contentResult.hasValue) {
                        var content: String? = null
                        if (contentResult.value is String) {
                            content = contentResult.value as String
                            if (content.isNotEmpty()) interests = JSONArray(content)
                        }
                        Log.d("old content", interests.toString())
                        if (content?.contains(user.blockstackId)!!) {
                            var i = 0
                            while (i < interests.length()) {
                                Log.d("i>>>>", interests[i] as String?)
                                val item = interests.getString(i)
                                if (item.equals(user.blockstackId)) interests.remove(i)
                                i++
                            }

                            Log.d("Final content", interests.toString())
                            val options_put = PutFileOptions(false)

                            blockstackSession().putFile("interest_page_0.json", interests.toString(), options_put)
                            { readURLResult ->
                                if (readURLResult.hasValue) {
                                    val readURL = readURLResult.value!!
                                    Log.d("Gaia URL", "File stored at: ${readURL}")
                                    userBox = ObjectBox.boxStore.boxFor(User::class.java)
                                    thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
                                    thoughtBox.remove(user.thoughts)
                                    userBox.remove(user.id)
                                    Log.d("removed", interests.toString())
                                    // [START subscribe_topics]
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + user.blockstackId)
                                            .addOnCompleteListener { task ->
                                                var msg = "Removed"
                                                if (!task.isSuccessful) {
                                                    msg = "Error in firebase"
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                    // [END subscribe_topics]
                                } else {
                                    Toast.makeText(context, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else
                            Log.d("Already removed", interests.toString())
                        mvpView?.hideLoading()
                    } else {
                        Toast.makeText(context, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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