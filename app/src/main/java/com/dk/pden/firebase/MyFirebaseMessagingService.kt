package com.dk.pden.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.dk.pden.App
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.discuss.DiscussActivity
import com.dk.pden.events.NewCommentEvent
import com.dk.pden.events.NewThoughtsEvent
import com.dk.pden.feed.FeedActivity
import com.dk.pden.model.*
import com.google.firebase.messaging.RemoteMessage
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.fcm.MessagingService
import io.objectbox.Box
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject


@SuppressLint("Registered")
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING")
class NotificationsMessagingService : MessagingService() {
    private lateinit var userBox: Box<User>
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var discussionBox: Box<Discussion>

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        discussionBox = ObjectBox.boxStore.boxFor(Discussion::class.java)

        // Check if message contains a data payload.
        remoteMessage.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            val thought = thoughtBox.query().equal(Thought_.uuid, remoteMessage.data["uuid"]).build().find()
            if (thought.isEmpty()) {
                val props = JSONObject()
                var user = userBox.query().equal(User_.blockstackId, remoteMessage.data["sender"]).build().findFirst()
                // If comment from unknown user from discussion topic
                if (user == null) {
                    user = User(remoteMessage.data["sender"]!!)
                    user.avatarImage = "https://api.adorable.io/avatars/285/" + user.blockstackId + ".png"
                }
                val thought = Thought(remoteMessage.data["text"]!!, remoteMessage.data["timestamp"]!!.toLong())
                thought.uuid = remoteMessage.data["uuid"]!!
                val mutableList: MutableList<Thought> = ArrayList()
                val discussion: Discussion
                val opted: Boolean
                // Comment for discussion topic
                if (remoteMessage.data.containsKey("topic")) {
                    val topic = remoteMessage.data["topic"].toString()
                    thought.isComment = true
                    val assert_conversation = discussionBox.query().equal(Discussion_.uuid, topic).build().find()
                    if (assert_conversation.isEmpty()) {
                        discussion = Discussion(topic)
                        // [START subscribe_topics]
                        PushNotifications.addDeviceInterest(topic)
                        // [END subscribe_topics]
                    } else
                        discussion = assert_conversation.first()

                    user.thoughts.add(thought)
                    userBox.put(user)
                    opted = discussion.thoughts.hasA { thought ->
                        thought.user.target.isSelf
                    }
                    thought.discussion.setAndPutTarget(discussion)
                    discussion.thoughts.add(thought)
                    discussionBox.put(discussion)
                    mutableList.add(thought)
                    EventBus.getDefault().post(NewCommentEvent(mutableList))
                    App.mixpanel.track("Comment received", props)
                }
                // Thought
                else {
                    // Spread thought from your interest
                    if (remoteMessage.data.containsKey("actual_owner")) {
                        var actual_owner = userBox.query().equal(User_.blockstackId, remoteMessage.data["actual_owner"]).build().findFirst()
                        thought.timestamp = remoteMessage.sentTime
                        if (actual_owner == null) {
                            actual_owner = User(remoteMessage.data["actual_owner"]!!)
                            actual_owner.avatarImage = "https://api.adorable.io/avatars/285/" + user.blockstackId + ".png"
                        }
                        actual_owner.thoughts.add(thought)
                        user.spreaded_thoughts.add(thought)
                        userBox.run {
                            put(user)
                            put(actual_owner)
                        }
                        thought.spreadBy.setAndPutTarget(user)
                        props.put("New", false)
                    } else {
                        // Fresh new thought
                        user.thoughts.add(thought)
                        userBox.put(user)
                        props.put("New", true)
                    }
                    mutableList.add(thought)
                    val discussion = Discussion(thought.uuid)
                    // [START subscribe_topics]
                    PushNotifications.addDeviceInterest(thought.uuid)
                    // [END subscribe_topics]
                    thought.discussion.setAndPutTarget(discussion)
                    discussion.thoughts.add(thought)
                    discussionBox.put(discussion)
                    opted = discussion.thoughts.hasA { thought ->
                        thought.user.target.isSelf
                    }
                    EventBus.getDefault().post(NewThoughtsEvent(mutableList))
                    App.mixpanel.track("Thought received", props)
                }

                if (opted or !thought.isComment)
                    sendNotification(this, thought)
                else
                    Log.d(TAG, "Not opted")

            } else {
                Log.d(TAG, "Already got the word")
            }
        }


    }
// [END receive_message]


    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.

//        // Get a instance of PreferencesHelper class
//        val preferencesHelper = PreferencesHelper(this)
//        // save token on preferences
//        preferencesHelper.registration_id = token
//
//        if (PreferencesHelper(this).blockstackId.isNotEmpty()) {
//            // Create a new comment
//            val users = HashMap<String, String?>()
//            users["registration_id"] = token
//            val db = FirebaseFirestore.getInstance()
//
//            // Add a new document with a generated ID
//            db.collection("users")
//                    .document(PreferencesHelper(this).blockstackId)
//                    .set(users as Map<*, *>)
//                    .addOnSuccessListener { documentReference ->
//                        Log.d(TAG, "DocumentSnapshot written with ID: $documentReference")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w(TAG, "Error adding document", e)
//                    }
//        }
    }

    companion object {

        private val TAG = "MyFirebaseMsgService"
        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param messageBody FCM message body received.
         */

        private fun sendNotification(notificationsMessagingService: NotificationsMessagingService, thought: Thought) {
            val intent: Intent

            if (thought.isComment) {
                intent = Intent(notificationsMessagingService, DiscussActivity::class.java)
                intent.putExtra("uuid", thought.discussion.target.uuid)

            } else {
                intent = Intent(notificationsMessagingService, FeedActivity::class.java)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(notificationsMessagingService, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT)

            val channelId = notificationsMessagingService.getString(R.string.default_notification_channel_id)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(notificationsMessagingService, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(thought.user.target.blockstackId + " posted new thought")
                    .setContentText(thought.textString)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)

            val notificationManager = notificationsMessagingService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel
                val name = notificationsMessagingService.getString(R.string.channel_name)
                val descriptionText = notificationsMessagingService.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel(channelId, name, importance)
                mChannel.description = descriptionText
                mChannel.enableLights(true)
                mChannel.lightColor = Color.YELLOW
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(mChannel)
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }
}