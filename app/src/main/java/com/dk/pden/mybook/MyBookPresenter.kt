package com.dk.pden.mybook

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.ObjectBox
import com.dk.pden.base.BasePresenter
import com.dk.pden.common.Utils.config
import com.dk.pden.events.NewThoughtsEvent
import com.dk.pden.events.RemoveThoughtsEvent
import com.dk.pden.model.Thought
import com.dk.pden.model.Thought_
import com.dk.pden.model.User
import com.dk.pden.model.User_
import com.dk.pden.shelf.ShelfActivity
import com.google.firebase.messaging.FirebaseMessaging
import io.objectbox.Box
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import org.blockstack.android.sdk.Result
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
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
                blockstackSession().getFile("kitab141.json", options) { contentResult ->
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
                            thought.uuid = item.getString("uuid")
                            thoughts.add(thought)
                            i++
                        }
                        thoughtBox.query().run {
                            equal(Thought_.userId, user.id)
                            build().remove()
                        }
                        user.thoughts.addAll(thoughts)
                        userBox.put(user)
                        mvpView?.showThoughts(thoughts.asReversed())
                        mvpView?.stopRefresh()
                    } else {
                        Toast.makeText(context, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
                options = GetFileOptions(username = user.blockstackId,
                        zoneFileLookupURL = zoneFileLookupUrl,
                        app = "https://app.pden.xyz",
                        decrypt = false)
                thoughts = mutableListOf()
                blockstackSession().lookupProfile(user.blockstackId, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
                    if (profileResult.hasValue) {
                        user.name = if (profileResult.value?.name != null) profileResult.value?.name!! else "-NA-"
                        user.description = if (profileResult.value?.description != null) profileResult.value?.description!! else "-NA-"
                        user.avatarImage = if (profileResult.value?.avatarImage != null) profileResult.value?.avatarImage!! else "https://s3.amazonaws.com/pden.xyz/avatar_placeholder.png"
                        userBox.put(user)
                        launch(UI) {
                            blockstackSession().getFile("kitab141.json", options) { contentResult: Result<Any> ->
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
                                        thought.uuid = item.getString("uuid")
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
                                    mvpView?.showThoughts(thoughts.asReversed())
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
        val props = JSONObject()
        userBox = ObjectBox.boxStore.boxFor(User::class.java)

        _blockstackSession = BlockstackSession(context, config
        ) {
            blockstackSession().getFile("pasand.json", options_get) { contentResult ->
                launch(UI) {
                    if (contentResult.hasValue) {
                        var content: String?
                        if (contentResult.value is String) {
                            content = contentResult.value as String
                            if (content.isNotEmpty()) interests = JSONArray(content)

                            if (!content.contains(user.blockstackId)) {
                                interests.put(user.blockstackId)
                                Log.d("Final content", interests.toString())
                                val options_put = PutFileOptions(false)

                                blockstackSession().putFile("pasand.json", interests.toString(), options_put)
                                { readURLResult ->
                                    if (readURLResult.hasValue) {
                                        val readURL = readURLResult.value!!
                                        val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
                                        var options = GetFileOptions(username = user.blockstackId,
                                                zoneFileLookupURL = zoneFileLookupUrl,
                                                app = "https://app.pden.xyz",
                                                decrypt = false)
                                        var thoughts = mutableListOf<Thought>()
                                        launch(UI) {
                                            blockstackSession().lookupProfile(user.blockstackId, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
                                                if (profileResult.hasValue) {
                                                    user.name = if (profileResult.value?.name != null) profileResult.value?.name!! else "-NA-"
                                                    user.description = if (profileResult.value?.description != null) profileResult.value?.description!! else "-NA-"
                                                    user.avatarImage = if (profileResult.value?.avatarImage != null) profileResult.value?.avatarImage!! else "https://s3.amazonaws.com/pden.xyz/avatar_placeholder.png"
                                                    userBox.put(user)
                                                    launch(UI) {
                                                        blockstackSession().getFile("kitab141.json", options) { contentResult: Result<Any> ->
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
                                                                    thought.uuid = item.getString("uuid")
                                                                    thoughts.add(thought)
                                                                    i++
                                                                }
                                                                Log.d("thoughts -> adding", my_book.toString())
                                                                userBox = ObjectBox.boxStore.boxFor(User::class.java)
                                                                user.thoughts.addAll(thoughts)
                                                                user.isFollowed = true
                                                                userBox.put(user)
                                                                props.put("Success", true)
                                                                if (thoughts.isNotEmpty())
                                                                    EventBus.getDefault().post(NewThoughtsEvent(thoughts))
                                                            } else {
                                                                props.put("Success", false)
                                                                val errorMsg = "error: " + contentResult.error
                                                                Log.d("errorMsg", errorMsg)
                                                            }
                                                            mixpanel.track("Borrow", props)
                                                            val intent = Intent(context, ShelfActivity::class.java)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                            context.startActivity(intent)
                                                        }
                                                    }

                                                } else {
                                                    props.put("Success", false)
                                                    mixpanel.track("Borrow", props)
                                                    val errorMsg = "error: " + profileResult.error
                                                    Log.d("errorMsg", errorMsg)
                                                    val intent = Intent(context, ShelfActivity::class.java)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    context.startActivity(intent)
                                                }
                                            }
                                        }

                                        Log.d("Gaia URL", "File stored at: ${readURL}")
                                    } else {
                                        props.put("Success", false)
                                        mixpanel.track("Borrow", props)
                                        Toast.makeText(context, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, ShelfActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        context.startActivity(intent)
                                    }
                                    // [START subscribe_topics]
                                    FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + user.blockstackId)
                                            .addOnCompleteListener { _ ->
                                                Toast.makeText(context, "Added to your shelf", Toast.LENGTH_SHORT).show()
                                            }
                                    // [END subscribe_topics]
                                    mvpView?.setBorrowed(true)
                                }

                            } else {
                                mvpView?.setBorrowed(true)
                                Toast.makeText(context, "Already added to your shelf", Toast.LENGTH_SHORT).show()
                            }
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
        val props = JSONObject()
        userBox = ObjectBox.boxStore.boxFor(User::class.java)


        _blockstackSession = BlockstackSession(context, config
        ) {
            blockstackSession().getFile("pasand.json", options_get) { contentResult ->
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

                            blockstackSession().putFile("pasand.json", interests.toString(), options_put)
                            { readURLResult ->
                                if (readURLResult.hasValue) {
                                    val readURL = readURLResult.value!!
                                    Log.d("Gaia URL", "File stored at: ${readURL}")
                                    userBox = ObjectBox.boxStore.boxFor(User::class.java)
                                    thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
                                    thoughtBox.remove(user.thoughts)
                                    userBox.remove(user.id)
                                    if (user.thoughts.isNotEmpty())
                                        EventBus.getDefault().post(RemoveThoughtsEvent(user.thoughts))
                                    props.put("Success", true)
                                } else {
                                    Toast.makeText(context, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
                                    props.put("Success", false)

                                }
                                mixpanel.track("UnBorrow", props)
                                mvpView?.setBorrowed(false)
                                val intent = Intent(context, ShelfActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                context.startActivity(intent)
                                // [START subscribe_topics]
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + user.blockstackId)
                                        .addOnCompleteListener { _ ->
                                            Toast.makeText(context, "Removed from your shelf", Toast.LENGTH_SHORT).show()
                                        }
                                // [END subscribe_topics]
                            }
                        } else {
                            mvpView?.setBorrowed(false)
                            mixpanel.track("UnBorrow", props)
                            val intent = Intent(context, ShelfActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            context.startActivity(intent)
                            Toast.makeText(context, "Already Removed from your shelf", Toast.LENGTH_SHORT).show()
                        }
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