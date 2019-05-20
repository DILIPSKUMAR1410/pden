package com.dk.pden

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils
import com.dk.pden.feed.FeedActivity
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.model.User_
import com.pusher.pushnotifications.PushNotifications
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import org.blockstack.android.sdk.Result
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class InitActivity : AppCompatActivity() {

    private var _blockstackSession: BlockstackSession? = null
    private lateinit var userBox: Box<User>
    var counter = 0
    var blockstack_id: String = ""
    private lateinit var progressPercent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)
        blockstack_id = PreferencesHelper(this).blockstackId
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progressPercent = findViewById(R.id.progressPercent)
        _blockstackSession = BlockstackSession(this, Utils.config) {
            // Wait until this callback fires before using any of the
            // BlockstackSession API methods
            val options = GetFileOptions(false)
            userBox = ObjectBox.boxStore.boxFor(User::class.java)
            blockstackSession().getFile("pasand.json", options) { contentResult ->
                var interests = JSONArray()
                if (contentResult.hasValue) {
                    val content: String?
                    if (contentResult.value is String) {
                        content = contentResult.value as String
                        if (content.isNotEmpty()) {
                            interests = JSONArray(content)
                            interests.put(blockstack_id)
                            // Default borrowed handles
                            interests.put("cryptoupdates.id.blockstack")
                            interests.put("scienceandtech.id.blockstack")
                            interests.put("amazingquotes.id.blockstack")

                            if (interests.length() > 0)
                                fetchBooks(interests, counter)
                            else
                                close()
                        }
                    } else {
                        val options_put = PutFileOptions(false)
                        GlobalScope.launch(Dispatchers.Main) {
                            blockstackSession().putFile("pasand.json", interests.toString(), options_put)
                            { readURLResult ->
                                if (readURLResult.hasValue) {
                                    interests.put(blockstack_id)
                                    // Default borrowed handles
                                    interests.put("cryptoupdates.id.blockstack")
                                    interests.put("scienceandtech.id.blockstack")
                                    interests.put("amazingquotes.id.blockstack")

                                    if (interests.length() > 0)
                                        fetchBooks(interests, counter)
                                    else
                                        close()
                                } else {
                                    throw IllegalStateException(readURLResult.error)
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
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

    fun close() {
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchBooks(interests: JSONArray, counter: Int) {
        val interest = interests.getString(counter)
        val percent = (counter * 100) / interests.length()
        val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
        val options = GetFileOptions(username = interest,
                zoneFileLookupURL = zoneFileLookupUrl,
                app = "https://app.pden.xyz",
                decrypt = false)
        GlobalScope.launch(Dispatchers.Main) {
            progressPercent.text = "$percent %"
            blockstackSession().lookupProfile(interest, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
                Log.d(">>>>>>>>", interest)
                val is_exist = profileResult.value?.json?.get("apps") as JSONObject
                if (profileResult.hasValue && is_exist.has("https://app.pden.xyz")) {
                    val user: User
                    if (interest.equals(blockstack_id)) {
                        user = userBox.query().equal(User_.blockstackId, blockstack_id).build().findFirst()!!
                    } else {
                        user = User(interest)
                        user.isFollowed = true
                    }

                    user.nameString = if (profileResult.value?.name != null) profileResult.value?.name!! else ""
                    user.description = if (profileResult.value?.description != null) profileResult.value?.description!! else ""
                    user.avatarImage = if (profileResult.value?.avatarImage != null) profileResult.value?.avatarImage!! else "https://api.adorable.io/avatars/285/" + user.blockstackId + ".png"
                    userBox.put(user)
                    GlobalScope.launch(Dispatchers.Main) {
                        blockstackSession().getFile("kitab141.json", options) { contentResult: Result<Any> ->
                            if (contentResult.hasValue) {
                                val content: Any
                                if (contentResult.value is String) {
                                    content = contentResult.value as String
                                    if (content.isNotEmpty()) {
                                        val interested_book = JSONArray(content)
                                        val thoughts = mutableListOf<Thought>()
                                        for (i in 0..(interested_book.length() - 1)) {
                                            val item = interested_book.getJSONObject(i)
                                            // Your code here
                                            val thought = Thought(item.getString("text"), item.getLong("timestamp"))
                                            thought.uuid = item.getString("uuid")
                                            thoughts.add(thought)
                                        }
                                        user.thoughts.addAll(thoughts)
                                        userBox.put(user)

                                        if (!user.isSelf) {
                                            // [START subscribe_topics]
                                            PushNotifications.addDeviceInterest(interest)
                                            if (counter == interests.length() - 1) {
                                                close()
                                            } else
                                                fetchBooks(interests, counter + 1)

                                            // [END subscribe_topics]
                                        } else {
                                            if (counter == interests.length() - 1) {
                                                close()
                                            } else
                                                fetchBooks(interests, counter + 1)
                                        }
                                    }
                                } else {
                                    val errorMsg = "error: Empty file"
                                    Log.d("errorMsg", errorMsg)
                                    if (counter == interests.length() - 1)
                                        close()
                                    else
                                        fetchBooks(interests, counter + 1)
                                }
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
