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
import com.dk.pen.model.MyThought
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_compose.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.PutFileOptions
import org.json.JSONObject
import java.sql.Timestamp

class ComposeThoughtActivity : AppCompatActivity(),ComposeThoughtMvpView {
    private val presenter: ComposeThoughtPresenter by lazy { ComposeThoughtPresenter() }
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var mythoughtBox: Box<MyThought>

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


        val config = java.net.URI("https://condescending-fermat-e43740.netlify.com").run {
            org.blockstack.android.sdk.BlockstackConfig(
                    this,
                    java.net.URI("${this}/redirect/"),
                    java.net.URI("${this}/manifest.json"),
                    kotlin.arrayOf(org.blockstack.android.sdk.Scope.StoreWrite))
        }

        _blockstackSession = BlockstackSession(this, config,
                onLoadedCallback = {
                    // Wait until this callback fires before using any of the
                    // BlockstackSession API methods
                })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_compose, menu)

        val item = menu.findItem(R.id.action_chars_left)
        val charsLeft = presenter.charsLeft()
        item.setTitle(charsLeft.toString())
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_send) {
            val options = PutFileOptions()
            val rootObject = JSONObject()
            when {
                presenter.charsLeft() == 140 -> showEmptyThoughtError()
                else -> {
                    rootObject.put("timestamp", Timestamp(System.currentTimeMillis()))
                    rootObject.put("thought", getThought())
                    blockstackSession().putFile("MyThoughts.json", rootObject.toString(), options,
                            { readURLResult ->
                                if (readURLResult.hasValue) {
                                    mythoughtBox = ObjectBox.boxStore.boxFor(MyThought::class.java)
                                    mythoughtBox.put(MyThought(rootObject.getString("thought"),rootObject.getString("timestamp")))
                                    val readURL = readURLResult.value!!
                                    Log.d("Gaia URL", "File stored at: ${readURL}")
                                } else {
                                    Toast.makeText(this, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
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
