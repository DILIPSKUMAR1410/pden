package com.dk.pen.compose

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.dk.pen.R
import kotlinx.android.synthetic.main.activity_compose.*

class ComposeThoughtActivity : AppCompatActivity(),ComposeThoughtMvpView {
    private val presenter: ComposeThoughtPresenter by lazy { ComposeThoughtPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener { finish() }
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



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.menu_compose, menu)

        val item = menu?.findItem(R.id.action_chars_left)
        MenuItemCompat.setActionView(item, R.layout.menu_chars_left)
        val view = MenuItemCompat.getActionView(item)

        val charsLeft = presenter.charsLeft()
        val charsLeftTextVIew = view.findViewById(R.id.charsLeftTextView) as TextView
        charsLeftTextVIew.text = charsLeft.toString()
        if (charsLeft < 0)
            charsLeftTextVIew.setTextColor(ContextCompat.getColor(this, R.color.primary_material_light))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_send)
            presenter.sendThought()
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

    override fun showEmptyTweetError() {
        Toast.makeText(this, "nothing_to_tweet", Toast.LENGTH_SHORT).show()
    }

    override fun showTooManyCharsError() {
        Toast.makeText(this, "too_many_characters", Toast.LENGTH_SHORT).show()
    }

    override fun setText(text: String?, selection: Int) {
        composeThoughtEditText.setText(text, TextView.BufferType.EDITABLE)
        composeThoughtEditText.setSelection(selection)
    }

}
