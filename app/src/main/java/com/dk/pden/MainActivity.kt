package com.dk.pden


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils.config
import com.dk.pden.common.visible
import com.dk.pden.model.User
import com.dk.pden.service.ApiServiceFactory
import com.pusher.pushnotifications.BeamsCallback
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PusherCallbackError
import com.pusher.pushnotifications.auth.AuthData
import com.pusher.pushnotifications.auth.AuthDataGetter
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_main.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.UserData

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var userBox: Box<User>
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var loadingProgressBar: ProgressBar
    private val apiService by lazy {
        ApiServiceFactory.createService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signInButton.isEnabled = false
        loadingProgressBar = findViewById(R.id.loadingProgressBar)


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

    @SuppressLint("CheckResult")
    private fun onSignIn(userData: UserData) {
        // Get a instance of PreferencesHelper class
        val preferencesHelper = PreferencesHelper(this)
        // save token on preferences
        preferencesHelper.blockstackId = userData.json.getString("username")

        userBox = ObjectBox.boxStore.boxFor(User::
        class.java)
        val user = User(userData.json.getString("username"))
        user.nameString = if (userData.profile?.name != null) userData.profile?.name!! else ""
        user.description = if (userData.profile?.description != null) userData.profile?.description!! else ""
        user.email = if (userData.profile?.email != null) userData.profile?.email!! else ""
        user.avatarImage = if (userData.profile?.avatarImage != null) userData.profile?.avatarImage!! else "https://api.adorable.io/avatars/285/" + user.blockstackId + ".png"
        user.isSelf = true
        userBox.put(user)


        val tokenProvider = BeamsTokenProvider(
                "https://pden.xyz/.netlify/functions/beam_token",
                object : AuthDataGetter {
                    override fun getAuthData(): AuthData {
                        return AuthData(
                                // Headers and URL query params your auth endpoint needs to
                                // request a Beams Token for a given user
                                headers = hashMapOf(
                                        // for example:
                                        // "Authorization" to sessionToken
                                ),
                                queryParams = hashMapOf("blockstack_id" to user.blockstackId)
                        )
                    }
                }
        )

        PushNotifications.setUserId(
                user.blockstackId,
                tokenProvider,
                object : BeamsCallback<Void, PusherCallbackError> {
                    override fun onFailure(error: PusherCallbackError) {
                        Log.e("BeamsAuth", "Could not login to Beams: ${error.message}");
                    }

                    override fun onSuccess(vararg values: Void) {
                        Log.i("BeamsAuth", "Beams login success");
                    }
                }
        )

        App.mixpanel.track("Login")
        App.mixpanel.identify(user.blockstackId)
        App.mixpanel.people.identify(user.blockstackId)
        App.mixpanel.people.increment("Login", 1.0)
        loadingProgressBar.visible(false)

        val intent = Intent(this, InitActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")
        signInButton.isEnabled = false
        loadingProgressBar.visible()

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
        Log.d(TAG, "response $response")
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

    private fun blockstackSession(): BlockstackSession {
        val session = _blockstackSession
        if (session != null) {
            return session
        } else {
            throw IllegalStateException("No session.")
        }
    }

}