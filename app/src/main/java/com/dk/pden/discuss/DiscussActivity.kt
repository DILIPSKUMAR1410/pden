package com.dk.pden.discuss

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.loadAvatar
import com.dk.pden.discuss.holder.CustomIncomingTextMessageViewHolder
import com.dk.pden.events.NewCommentEvent
import com.dk.pden.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_discuss2.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DiscussActivity : AppCompatActivity(), MessagesListAdapter.SelectionListener, MessageInput.InputListener,
        MessageInput.TypingListener {

    private lateinit var discussionBox: Box<Discussion>
    private lateinit var userBox: Box<User>
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var adapter: MessagesListAdapter<Thought>
    private lateinit var user: User
    private lateinit var imageLoader: ImageLoader
    private lateinit var db: FirebaseFirestore
    private var blockstack_id = ""
    private val TAG = "Discussion"
    private var selectionCount: Int = 0
    private var menu: Menu? = null


    companion object {

        private var uuid: String = ""
        fun launch(context: Context, uuid: String) {
            val intent = Intent(context, DiscussActivity::class.java)
            intent.putExtra("uuid", uuid)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discuss2)
        // Get the support action bar
        val actionBar = supportActionBar

        // Set the action bar title, subtitle and elevation
        actionBar!!.title = "Discuss (v0.1)"
        actionBar.elevation = 4.0F
        actionBar.setDisplayHomeAsUpEnabled(true)
        blockstack_id = PreferencesHelper(this).blockstackId
        db = FirebaseFirestore.getInstance()
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        discussionBox = ObjectBox.boxStore.boxFor(Discussion::class.java)
        userBox = ObjectBox.boxStore.boxFor(User::class.java)

        user = userBox.find(User_.blockstackId, blockstack_id).first()
        imageLoader = ImageLoader { imageView, url, _ -> imageView.loadAvatar(url) }


        val holdersConfig = MessageHolders()
                .setIncomingTextConfig(
                        CustomIncomingTextMessageViewHolder::class.java,
                        R.layout.item_custom_incoming_text_message,
                        null)
        adapter = MessagesListAdapter(blockstack_id, holdersConfig, imageLoader)
        adapter.enableSelectionMode(this)
        messagesList.setAdapter(adapter)
        uuid = intent.getStringExtra("uuid")
        val conversation = discussionBox.find(Discussion_.uuid, uuid).firstOrNull()
        if (conversation == null) {
            adapter.addToEnd(thoughtBox.find(Thought_.uuid, uuid), true)
        } else
            adapter.addToEnd(conversation.thoughts, true)

        input.setInputListener(this)
        input.setTypingListener(this)
    }

    override fun onSelectionChanged(count: Int) {
        selectionCount = count
        menu?.findItem(R.id.action_public)?.isVisible = count > 0
    }

    override fun onSubmit(input: CharSequence): Boolean {

        val thought = Thought(input.toString(), System.currentTimeMillis())
        thought.isComment = true

        // Create a new comment
        val comment = HashMap<String, Any>()
        comment["timestamp"] = thought.timestamp
        comment["text"] = thought.textString
        comment["uuid"] = thought.uuid
        comment["actual_owner"] = blockstack_id

        var conversation = discussionBox.find(Discussion_.uuid, uuid).firstOrNull()
        if (conversation == null) {
            conversation = Discussion(uuid)
            // [START subscribe_topics]
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + thought.uuid)
            // [END subscribe_topics]
        }
        user.thoughts.add(thought)
        userBox.put(user)
        conversation.thoughts.add(thought)
        discussionBox.put(conversation)
        adapter.addToStart(thought, true)


        // Add a new document with a generated ID
        db.collection("thoughts").document(uuid).collection("discussion")
                .add(comment)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

//        props.put("Success", true)
//        App.mixpanel.track("Comment", props)
//        App.mixpanel.people.increment("Comment", 1.0)
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddEvent(event: NewCommentEvent) {
        /* Do something */
        adapter.addToStart(event.thoughts.get(0), true)

    }

    override fun onCreateOptionsMenu(cmenu: Menu): Boolean {
        menu?.clear()
        menu = cmenu
        menuInflater.inflate(R.menu.menu_discussion, menu)
        onSelectionChanged(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_public -> {
                val docsref = db.collection("thoughts").document(uuid).collection("discussion")
                docsref.get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                if (document.data["uuid"] in adapter.selectedMessages.map { it.uuid }) {
                                    Log.d(TAG, document.id + " => " + document.data["uuid"])
                                    document.data["isApproved"] = true
                                    docsref.document(document.id)
                                            .update("isApproved", true)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ", exception)
                        }
            }
        }
        return true
    }

    public override fun onStart() {
        super.onStart()
        Log.d("Eventbus -->>", "regiser")
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
//        App.mixpanel.track("Discuss");
    }

    fun close() {
        finish()
    }

    override fun onStartTyping() {
        Log.v("Typing listener", ">>>")
    }

    override fun onStopTyping() {
        Log.v("Typing listener", ">>>")
    }


}
