package com.dk.pen


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dk.pen.common.PreferencesHelper
import com.dk.pen.common.Utils.config
import com.dk.pen.model.User
import com.dk.pen.model.User_
import com.dk.pen.shelf.ShelfActivity
import io.objectbox.Box
import kotlinx.android.synthetic.main.content_main.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.UserData

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var userBox: Box<User>
    private var _blockstackSession: BlockstackSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get a instance of PreferencesHelper class
        userBox = ObjectBox.boxStore.boxFor(User::class.java)

        if (userBox.find(User_.blockstackId, PreferencesHelper(this).deviceToken).isNotEmpty()) {
            val intent = Intent(this, ShelfActivity::class.java)
            startActivity(intent)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        signInButton.isEnabled = false
//        getStringFileButton.isEnabled = false
//        putStringFileButton.isEnabled = false

        _blockstackSession = BlockstackSession(this, config,
                onLoadedCallback = {
                    // Wait until this callback fires before using any of the
                    // BlockstackSession API methods
                    signInButton.isEnabled = true
                })
//        getStringFileButton.isEnabled = false
//        putStringFileButton.isEnabled = false
//        getImageFileButton.isEnabled = false
//        putImageFileButton.isEnabled = false
//        getStringFileFromUserButton.isEnabled = false

        signInButton.setOnClickListener { _: View ->
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

//        getStringFileButton.setOnClickListener { _ ->
//            fileContentsTextView.text = "Downloading..."
//            val options = GetFileOptions()
//            blockstackSession().getFile(textFileName, options, { contentResult ->
//                if (contentResult.hasValue) {
//                    val content = contentResult.value!!.toString()
//                    Log.d(TAG, "File contents: ${content as String}")
//                    runOnUiThread {
//                        fileContentsTextView.text = content
//                    }
//                } else {
//                    Toast.makeText(this, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//
//        putStringFileButton.setOnClickListener { _ ->
//            readURLTextView.text = "Uploading..."
//            val options = PutFileOptions()
//            val rootObject= JSONObject()
//            rootObject.put("name","Dilip")
//            rootObject.put("age","28")
//            blockstackSession().putFile(textFileName, rootObject.toString(), options,
//                    { readURLResult ->
//                        if (readURLResult.hasValue) {
//                            val readURL = readURLResult.value!!
//                            Log.d(TAG, "File stored at: ${readURL}")
//                            runOnUiThread {
//                                readURLTextView.text = "File stored at: ${readURL}"
//                            }
//                        } else {
//                            Toast.makeText(this, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
//                        }
//                    })
//        }
//
//        putImageFileButton.setOnClickListener { _ ->
//            imageFileTextView.text = "Uploading..."
//
//            val drawable: BitmapDrawable = resources.getDrawable(R.drawable.blockstackteam) as BitmapDrawable
//
//            val bitmap = drawable.getBitmap()
//            val stream = ByteArrayOutputStream()
//
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            val bitMapData = stream.toByteArray()
//
//            val options = PutFileOptions(false)
//            blockstackSession().putFile(imageFileName, bitMapData, options,
//                    { readURLResult ->
//                        if (readURLResult.hasValue) {
//                            val readURL = readURLResult.value!!
//                            Log.d(TAG, "File stored at: ${readURL}")
//                            runOnUiThread {
//                                imageFileTextView.text = "File stored at: ${readURL}"
//                            }
//                        } else {
//                            Toast.makeText(this, "error: " + readURLResult.error, Toast.LENGTH_SHORT).show()
//                        }
//                    })
//        }
//
//        getImageFileButton.setOnClickListener { _ ->
//            val options = GetFileOptions(decrypt = false)
//            blockstackSession().getFile(imageFileName, options, { contentsResult ->
//                if (contentsResult.hasValue) {
//                    val contents = contentsResult.value!!
//                    val imageByteArray = contents as ByteArray
//                    val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
//                    runOnUiThread {
//                        imageView.setImageBitmap(bitmap)
//                    }
//                } else {
//                    Toast.makeText(this, "error: " + contentsResult.error, Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//
//        getStringFileFromUserButton.setOnClickListener { _ ->
//            val username = "dev_android_sdk.id.blockstack";
//            val zoneFileLookupUrl = URL("https://core.blockstack.org/v1/names")
//            fileFromUserContentsTextView.text = "Downloading file from other user..."
//            blockstackSession().lookupProfile(username, zoneFileLookupURL = zoneFileLookupUrl) { profileResult ->
//                if (profileResult.hasValue) {
//                    val profile = profileResult.value!!
//                    runOnUiThread {
//                        val options = GetFileOptions(username = username,
//                                zoneFileLookupURL = zoneFileLookupUrl,
//                                app = "https://condescending-fermat-e43740.netlify.com",
//                                decrypt = false)
//                        blockstackSession().getFile(textFileName, options, { contentResult: Result<Any> ->
//                            if (contentResult.hasValue) {
//                                val content = contentResult.value!!
//                                runOnUiThread {
//                                    fileFromUserContentsTextView.text = "from ${profile.name}($username):\n ${content as String}"
//                                }
//                            } else {
//                                val errorMsg = "error: " + contentResult.error
//                                fileFromUserContentsTextView.text = errorMsg
//                                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
//                            }
//                        })
//                    }
//                } else {
//                    val errorMsg = "error: " + profileResult.error
//                    fileFromUserContentsTextView.text = errorMsg
//                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    private fun onSignIn(userData: UserData) {
        signInButton.isEnabled = false
        // Get a instance of PreferencesHelper class
        val preferencesHelper = PreferencesHelper(this)
        // save token on preferences
        preferencesHelper.deviceToken = userData.json.getString("username")
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        val user = User(userData.json.getString("username"))
        user.name = if (userData.profile?.name != null) userData.profile?.name!! else "-NA-"
        user.description = if (userData.profile?.description != null) userData.profile?.description!! else "-NA-"
        user.avatarImage = if (userData.profile?.avatarImage != null) userData.profile?.avatarImage!! else "-NA-"
        userBox.put(user)

        val intent = Intent(this, ShelfActivity::class.java)
        startActivity(intent)

//        userDataTextView.text = "Signed in as ${userData.decentralizedID}"
//        showUserAvatar(userData.profile?.avatarImage)
//        getStringFileButton.isEnabled = true
//        putStringFileButton.isEnabled = true
//        putImageFileButton.isEnabled = true
//        getImageFileButton.isEnabled = true
//        getStringFileFromUserButton.isEnabled = true
    }

//    private fun showUserAvatar(avatarImage: String?) {
//        if (avatarImage != null) {
//            // use whatever suits your app architecture best to asynchronously load the avatar
//            // better use a image loading library than the code below
//            async(UI) {
//                val avatar = bg {
//                    try {
//                        BitmapDrawable.createFromStream(URL(avatarImage).openStream(), "src")
//                    } catch (e: Exception) {
//                        Log.d(TAG, e.toString())
//                        null
//                    }
//                }.await()
//                avatarView.setImageDrawable(avatar)
//            }
//        } else {
//            avatarView.setImageResource(R.drawable.default_avatar)
//        }
//    }

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