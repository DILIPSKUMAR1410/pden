package com.dk.pden


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils.config
import com.dk.pden.model.User
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_main.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.UserData

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var userBox: Box<User>
    private var _blockstackSession: BlockstackSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signInButton.isEnabled = false


        _blockstackSession = BlockstackSession(this, config,
                onLoadedCallback = {
                    // Wait until this callback fires before using any of the
                    // BlockstackSession API methods
                    signInButton.isEnabled = true
                })

        signInButton.setOnClickListener {
            blockstackSession().redirectUserToSignIn { userDataResult ->
                if (userDataResult.hasValue) {
                    Log.d(TAG, "signed in!")
                    runOnUiThread {
                        onSignIn(userDataResult.value!!)
                    }
                } else {
                    Toast.makeText(this, "error: " + userDataResult.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onSignIn(userData: UserData) {
        signInButton.isEnabled = false
        // Get a instance of PreferencesHelper class
        val preferencesHelper = PreferencesHelper(this)
        // save token on preferences
        preferencesHelper.blockstackId = userData.json.getString("username")
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        val user = User(userData.json.getString("username"))
        user.name = if (userData.profile?.name != null) userData.profile?.name!! else ""
        user.description = if (userData.profile?.description != null) userData.profile?.description!! else ""
        user.avatarImage = if (userData.profile?.avatarImage != null) userData.profile?.avatarImage!! else "https://s3.amazonaws.com/pden.xyz/avatar_placeholder.png"
        user.isSelf = true
        userBox.put(user)
        mixpanel.track("Login")
        mixpanel.identify(user.blockstackId)
        mixpanel.people.identify(user.blockstackId)
        mixpanel.people.increment("Login", 1.0)
        val intent = Intent(this, InitActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")

        if (intent?.action == Intent.ACTION_MAIN) {
            blockstackSession().loadUserData { userData ->
                if (userData != null) {
                    runOnUiThread {
                        onSignIn(userData)
                    }
                } else {
                    Toast.makeText(this, "no user data", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (intent?.action == Intent.ACTION_VIEW) {
            handleAuthResponse(intent)
        }
    }

    private fun handleAuthResponse(intent: Intent) {
        val response = intent.dataString
        Log.d(TAG, "response ${response}")
        if (response != null) {
            val authResponseTokens = response.split(':')
            if (authResponseTokens.size > 1) {
                val authResponse = authResponseTokens[1]
                Log.d(TAG, "authResponse: ${authResponse}")
                blockstackSession().handlePendingSignIn(authResponse) { userDataResult ->
                    if (userDataResult.hasValue) {
                        val userData = userDataResult.value!!
                        Log.d(TAG, "signed in!")
                        runOnUiThread {
                            onSignIn(userData)
                        }
                    } else {
                        Log.d(TAG, "error: " + userDataResult.error)
                        Toast.makeText(this, "error: " + userDataResult.error, Toast.LENGTH_SHORT).show()
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