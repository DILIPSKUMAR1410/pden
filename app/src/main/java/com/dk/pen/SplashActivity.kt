package com.dk.pen

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dk.pen.common.PreferencesHelper
import com.dk.pen.model.User
import com.dk.pen.model.User_
import com.dk.pen.shelf.ShelfActivity
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
        if (userBox.find(User_.blockstackId, PreferencesHelper(this).blockstackId).isNotEmpty()) {
            intent = Intent(this, ShelfActivity::class.java)
        } else {
            intent = Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
