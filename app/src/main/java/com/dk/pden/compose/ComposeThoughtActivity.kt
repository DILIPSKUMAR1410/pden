package com.dk.pden.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils
import com.dk.pden.common.visible
import com.dk.pden.events.NewThoughtsEvent
import com.dk.pden.model.Discussion
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.model.User_
import com.pusher.pushnotifications.PushNotifications
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_compose.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject


class ComposeThoughtActivity : AppCompatActivity(), ComposeThoughtMvpView {
    private val presenter: ComposeThoughtPresenter by lazy { ComposeThoughtPresenter() }
    private var _blockstackSession: BlockstackSession? = null
    private var blockstack_id: String = ""

    private lateinit var userBox: Box<User>
    private lateinit var discussionBox: Box<Discussion>
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var preferencesHelper: PreferencesHelper
    private val TAG = "ComposeActivity"


    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ComposeThoughtActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)
        preferencesHelper = PreferencesHelper(this)
        blockstack_id = preferencesHelper.blockstackId

        // Get the support action bar
        val actionBar = supportActionBar

        actionBar!!.title = "Compose"

        // Set the action bar title, subtitle and elevation
        actionBar.elevation = 4.0F
        actionBar.setDisplayHomeAsUpEnabled(true)


        presenter.attachView(this)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        discussionBox = ObjectBox.boxStore.boxFor(Discussion::class.java)
        mixpanel.timeEvent("Compose");

        composeThoughtEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                presenter.afterTextChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onTextChanged(s.toString(), start, count)
            }
        })


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_compose, menu)
        val item = menu.findItem(R.id.action_chars_left)
        val charsLeft = presenter.charsLeft()
        item.title = charsLeft.toString()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_send) {
            when (item.itemId == R.id.action_send) {
                presenter.charsLeft() == 256 -> showEmptyThoughtError()
                else -> {
                    val rootObject = JSONObject()
                    val props = JSONObject()
                    item.isEnabled = false
                    composeThoughtEditText.isEnabled = false
                    showLoading()
                    val user = userBox.query().equal(User_.blockstackId, blockstack_id).build().findFirst()
                    val thought = Thought(getThought(), System.currentTimeMillis())
                    thought.isComment = false
                    rootObject.put("timestamp", thought.timestamp)
                    rootObject.put("text", thought.textString)
                    rootObject.put("uuid", thought.uuid)
                    var my_book = JSONArray()
                    _blockstackSession = BlockstackSession(this, Utils.config
                    ) {
                        // Wait until this callback fires before using any of the
                        // BlockstackSession API methods
                        val options_get = GetFileOptions(false)
                        blockstackSession().getFile("kitab141.json", options_get) { contentResult ->
                            if (contentResult.hasValue) {
                                val content: Any
                                if (contentResult.value is String) {
                                    content = contentResult.value as String
                                    if (content.isNotEmpty()) {
                                        my_book = JSONArray(content)
                                    }
                                }
                                my_book.put(rootObject)
                                val options_put = PutFileOptions(false)
                                runOnUiThread {
                                    blockstackSession().putFile("kitab141.json", my_book.toString(), options_put)
                                    { readURLResult ->
                                        if (readURLResult.hasValue) {
                                            user!!.thoughts.add(thought)
                                            // [START subscribe_topics]
                                            PushNotifications.addDeviceInterest(thought.uuid)
                                                    .let {
                                                        userBox.put(user)
                                                        val conversation = Discussion(thought.uuid)
                                                        conversation.thoughts.add(thought)
                                                        discussionBox.put(conversation)
                                                        presenter.sendThought(blockstack_id, rootObject)
                                                        val mutableList: MutableList<Thought> = ArrayList()
                                                        mutableList.add(thought)
                                                        if (mutableList.isNotEmpty())
                                                            EventBus.getDefault().post(NewThoughtsEvent(mutableList))
                                                        close()
                                                    }
                                            // [END subscribe_topics]
                                        } else {
                                            props.put("Success", false)
                                            Toast.makeText(this, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
                                        }
                                        props.put("Success", true)
                                        mixpanel.track("Post", props)
                                        mixpanel.people.increment("Post", 1.0)

                                    }
                                }

                            } else {
                                props.put("Success", false)
                                mixpanel.track("Post", props)
                                Toast.makeText(this, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } else {
            close()
        }
        return true
    }

    override fun refreshToolbar() {
        invalidateOptionsMenu()
    }

    override fun close() {
        mixpanel.track("Compose")
        finish()
    }

    override fun showLoading() {
        loadingProgressBar.visible(true)
    }

    override fun getThought() = composeThoughtEditText.text.toString().trim().replace("\"", "").replace("\'", "")


    override fun showSendTweetError() {
        Toast.makeText(this, "sending_message_error", Toast.LENGTH_SHORT).show()
    }

    override fun showEmptyThoughtError() {
        Toast.makeText(this, "Nothing to post", Toast.LENGTH_SHORT).show()
    }

    override fun showTooManyCharsError() {
        Toast.makeText(this, "too_many_characters", Toast.LENGTH_SHORT).show()
    }

    override fun setText(text: String?, selection: Int) {
        composeThoughtEditText.setText(text, TextView.BufferType.EDITABLE)
        composeThoughtEditText.setSelection(selection)
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
