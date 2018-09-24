package com.dk.pen.compose

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.dk.pen.ObjectBox
import com.dk.pen.R
import com.dk.pen.common.PreferencesHelper
import com.dk.pen.common.Utils
import com.dk.pen.model.Thought
import com.dk.pen.model.User
import com.dk.pen.model.User_
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_compose.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import org.json.JSONArray
import org.json.JSONObject

class ComposeThoughtActivity : AppCompatActivity(), ComposeThoughtMvpView {
    private val presenter: ComposeThoughtPresenter by lazy { ComposeThoughtPresenter() }
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var userBox: Box<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        // Get the support action bar
        val actionBar = supportActionBar

        // Set the action bar title, subtitle and elevation
        actionBar!!.title = "Compose"
        actionBar.elevation = 4.0F
        presenter.attachView(this)

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
            var my_book = JSONArray()
            val rootObject = JSONObject()
            when {
                presenter.charsLeft() == 140 -> showEmptyThoughtError()
                else -> {
                    rootObject.put("timestamp", System.currentTimeMillis())
                    rootObject.put("text", getThought())
                    _blockstackSession = BlockstackSession(this, Utils.config,
                            onLoadedCallback = {
                                // Wait until this callback fires before using any of the
                                // BlockstackSession API methods
                                val options_get = GetFileOptions(false)
                                blockstackSession().getFile("book.json", options_get) { contentResult ->
                                    if (contentResult.hasValue) {
                                        val content: Any
                                        if (contentResult.value is String) {
                                            content = contentResult.value as String
                                            if (content.isNotEmpty()) {
                                                my_book = JSONArray(content)
                                            }
                                        }
                                        Log.d("old content", my_book.toString())
                                        my_book.put(rootObject)
                                        Log.d("Final content", my_book.toString())
                                        val options_put = PutFileOptions(false)
                                        runOnUiThread {
                                            blockstackSession().putFile("book.json", my_book.toString(), options_put)
                                            { readURLResult ->
                                                if (readURLResult.hasValue) {
                                                    userBox = ObjectBox.boxStore.boxFor(User::class.java)
                                                    val blockstack_id = PreferencesHelper(this).blockstackId
                                                    val user = userBox.find(User_.blockstackId, blockstack_id).first()
                                                    val thought = Thought(rootObject.getString("text"), rootObject.getString("timestamp").toLong())
                                                    user.thoughts.add(thought)
                                                    userBox.put(user)
                                                    presenter.sendThought(blockstack_id, rootObject)
//                                        KBus.post(NewMyThoughtEvent(thought))
                                                    close()
                                                } else {
                                                    Toast.makeText(this, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                    } else {
                                        Toast.makeText(this, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })

                }
            }
        }
        return true
    }

    override fun refreshToolbar() {
        invalidateOptionsMenu()
    }

    override fun close() {
        finish()
    }

    override fun getThought() = composeThoughtEditText.text.toString()

    override fun showSendTweetError() {
        Toast.makeText(this, "sending_message_error", Toast.LENGTH_SHORT).show()
    }

    override fun showEmptyThoughtError() {
        Toast.makeText(this, "nothing_to_tweet", Toast.LENGTH_SHORT).show()
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
