package com.dk.pden.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.dk.pden.App
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.conversation.ConversationActivity
import com.dk.pden.events.NewCommentEvent
import com.dk.pden.events.NewThoughtsEvent
import com.dk.pden.feed.FeedActivity
import com.dk.pden.model.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.objectbox.Box
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var userBox: Box<User>
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var conversationBox: Box<Conversation>

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
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
        Log.d(TAG, "From: ${remoteMessage?.from}")

        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        conversationBox = ObjectBox.boxStore.boxFor(Conversation::class.java)

        // Check if message contains a data payload.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            val topic = remoteMessage.from?.removePrefix("/topics/")
            val user = userBox.find(User_.blockstackId, topic).firstOrNull()
            val isComment = user == null
            val thought = thoughtBox.find(Thought_.uuid, remoteMessage.data.get("uuid"))
            if (thought.isEmpty()) {
                val thought = Thought(remoteMessage.data.get("text")!!, remoteMessage.data.get("timestamp")!!.toLong())
                thought.uuid = remoteMessage.data.get("uuid")!!
                thought.isComment = isComment
                val props = JSONObject()
                if (remoteMessage.data.containsKey("actual_owner")) {
                    var actual_owner = userBox.find(User_.blockstackId, remoteMessage.data["actual_owner"]).firstOrNull()
                    if (actual_owner == null) {
                        actual_owner = User(remoteMessage.data.get("actual_owner")!!)
                        actual_owner.avatarImage = "https://s3.amazonaws.com/pden.xyz/avatar_placeholder.png"
                    }
                    actual_owner.thoughts.add(thought)
                    if (!isComment) user!!.spreaded_thoughts.add(thought)
                    userBox.put(actual_owner)
                    props.put("New", false)
                } else {
                    user!!.thoughts.add(thought)
                    userBox.put(user)
                    props.put("New", true)
                }
                val mutableList: MutableList<Thought> = ArrayList()
                mutableList.add(thought)
                if (isComment) {
                    val conversation = conversationBox.find(Conversation_.uuid, topic).firstOrNull()
                    if (conversation != null) {
                        thought.conversation.setAndPutTarget(conversation)
                        conversation.thoughts.add(thought)
                        conversationBox.put(conversation)
                        EventBus.getDefault().post(NewCommentEvent(mutableList))
                        App.mixpanel.track("Comment received", props)
                    }
                } else {
                    EventBus.getDefault().post(NewThoughtsEvent(mutableList))
                    App.mixpanel.track("Thought received", props)
                }
                Companion.sendNotification(this, thought)

            } else {
                Log.d(TAG, "Already got the word")
            }


//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob()
//            } else {
//                // Handle message within 10 seconds
//                handleNow()
//            }
            // Check if message contains a notification payload.
//            remoteMessage?.notification?.let
//            {
//                Log.d(TAG, "Message Notification Body: ${it.body}")
//            }

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
        }


    }
// [END receive_message]


// [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
// [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
//        val myJob = dispatcher.newJobBuilder()
//                .setService(MyJobService::class.java)
//                .setTag("my-job-tag")
//                .build()
//        dispatcher.schedule(myJob)
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
    }

    companion object {

        private val TAG = "MyFirebaseMsgService"
        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param messageBody FCM message body received.
         */
        private fun sendNotification(myFirebaseMessagingService: MyFirebaseMessagingService, thought: Thought) {
            val intent: Intent

            if (thought.isComment) {
                intent = Intent(myFirebaseMessagingService, ConversationActivity::class.java)
                intent.putExtra("uuid", thought.conversation.target.uuid)

            } else {
                intent = Intent(myFirebaseMessagingService, FeedActivity::class.java)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(myFirebaseMessagingService, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT)

            val channelId = "Pden"
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(myFirebaseMessagingService, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(thought.user.target.blockstackId + " posted new thought")
                    .setContentText(thought.text)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)

            val notificationManager = myFirebaseMessagingService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel
                val name = myFirebaseMessagingService.getString(R.string.channel_name)
                val descriptionText = myFirebaseMessagingService.getString(R.string.channel_description)
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