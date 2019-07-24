package com.dk.pden

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.feed.FeedActivity
import com.dk.pden.model.User
import com.dk.pden.model.User_
import io.objectbox.Box

class SplashActivity : AppCompatActivity() {
    private lateinit var userBox: Box<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get a instance of PreferencesHelper class
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        val intent: Intent
        if (userBox.query().equal(User_.blockstackId, PreferencesHelper(this).blockstackId).build().find().isNotEmpty()) {
            // Get a instance of PreferencesHelper class
            val preferencesHelper = PreferencesHelper(this)
            // save token on preferences
            if (preferencesHelper.isInitCompleted) {
                intent = Intent(this, FeedActivity::class.java)
            } else {
                intent = Intent(this, InitActivity::class.java)
            }
        } else {
            intent = Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
