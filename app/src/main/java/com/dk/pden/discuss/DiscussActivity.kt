package com.dk.pden.discuss

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dk.pden.App
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.loadAvatar
import com.dk.pden.discuss.holder.CustomIncomingTextMessageViewHolder
import com.dk.pden.events.NewCommentEvent
import com.dk.pden.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.pusher.pushnotifications.PushNotifications
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.objectbox.Box
import kotlinx.android.synthetic.main.activity_discuss.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class DiscussActivity : AppCompatActivity(), MessagesListAdapter.SelectionListener, MessageInput.InputListener,
        MessageInput.TypingListener {
    private val presenter: DiscussPresenter by lazy { DiscussPresenter() }

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
    //We can pass any data to ViewHolder with payload
    val payload = CustomIncomingTextMessageViewHolder.Payload();

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
        setContentView(R.layout.activity_discuss)
        // Get the support action bar
        val actionBar = supportActionBar

        // Set the action bar title, subtitle and elevation
        actionBar!!.title = "Discuss (v0.2)"
        actionBar.elevation = 4.0F
        actionBar.setDisplayHomeAsUpEnabled(true)
        blockstack_id = PreferencesHelper(this).blockstackId
        db = FirebaseFirestore.getInstance()
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        discussionBox = ObjectBox.boxStore.boxFor(Discussion::class.java)
        userBox = ObjectBox.boxStore.boxFor(User::class.java)

        user = userBox.find(User_.blockstackId, blockstack_id).first()
        imageLoader = ImageLoader { imageView, url, _ -> imageView.loadAvatar(url) }


        uuid = intent.getStringExtra("uuid")


        //For example click listener
        payload.iamAdmin = thoughtBox.find(Thought_.uuid, uuid).first().user.targetId == user.pk


        val holdersConfig = MessageHolders()
                .setIncomingTextConfig(
                        CustomIncomingTextMessageViewHolder::class.java,
                        R.layout.item_custom_incoming_text_message,
                        payload)
        adapter = MessagesListAdapter(blockstack_id, holdersConfig, imageLoader)
        if (payload.iamAdmin) {
            adapter.enableSelectionMode(this)
        }
        messagesList.setAdapter(adapter)
        var conversation = discussionBox.find(Discussion_.uuid, uuid).firstOrNull()
        if (conversation == null) {
            conversation = Discussion(uuid)
            val docsref = db.collection("thoughts").document(uuid).collection("discussion")
            docsref.get()
                    .addOnSuccessListener { result ->
                        val actual_owners = mutableListOf<User>()
                        for (document in result) {
                            var thought: Thought

                            if (document.data.containsKey("uuid")) {
                                thought = Thought(document.data["text"] as String, document.data["timestamp"].toString().toLong())
                                thought.uuid = document.data["uuid"] as String
                                thought.isComment = true
                                if (document.data.containsKey("isApproved"))
                                    thought.isApproved = document.data["isApproved"] as Boolean
                                var actual_owner = userBox.find(User_.blockstackId, document.data["actual_owner"] as String).firstOrNull()
                                if (actual_owner == null) {
                                    actual_owner = User(document.data["actual_owner"] as String)
                                    actual_owner.avatarImage = "https://ui-avatars.com/api/?background=8432F8&color=F5C227&rounded=true&name=${actual_owner.blockstackId}"
                                }
                                actual_owner.thoughts.add(thought)
                                actual_owners.add(actual_owner)
                            } else {
                                thought = thoughtBox.find(Thought_.uuid, uuid).first()
                            }
                            conversation.thoughts.add(thought)

                        }
                        userBox.put(actual_owners)
                        discussionBox.put(conversation)
                        adapter.addToEnd(conversation.thoughts.sortedByDescending { it -> it.timestamp }, false)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                    }
        } else
            adapter.addToEnd(conversation.thoughts.sortedByDescending { it -> it.timestamp }, false)



        input.setInputListener(this)
        input.setTypingListener(this)
    }

    override fun onSelectionChanged(count: Int) {
        this.selectionCount = count;
        menu?.findItem(R.id.selected_count)?.title = selectionCount.toString()
        menu?.findItem(R.id.action_public)?.isVisible = selectionCount > 0
        menu?.findItem(R.id.selected_count)?.isVisible = selectionCount > 0

    }

    override fun onSubmit(input: CharSequence): Boolean {

        val thought = Thought(input.toString(), System.currentTimeMillis())
        thought.isComment = true
        val props = JSONObject()

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
            PushNotifications.addDeviceInterest(thought.uuid);
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
                .addOnSuccessListener {
                    presenter.sendComment(user.blockstackId, uuid, comment)
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    props.put("Success", true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    props.put("Failure", true)
                }

        App.mixpanel.track("Comment", props)
        App.mixpanel.people.increment("Comment", 1.0)
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
        menu?.findItem(R.id.action_public)?.isVisible = false
        menu?.findItem(R.id.selected_count)?.isVisible = false
        onSelectionChanged(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_public) {
            when (item.itemId) {
                R.id.action_public -> {
                    val y = ArrayList<Thought>()
                    adapter.selectedMessages.forEach { x ->
                        if (!x.isApproved and !x.user.target.isSelf) {
                            y.add(x)
                        }
                    }
                    val uuids = y.map { it -> it.uuid }
                    if (uuids.isNotEmpty()) {
                        val docsref = db.collection("thoughts").document(uuid).collection("discussion")
                        docsref.get()
                                .addOnSuccessListener { result ->
                                    var count = 0
                                    for (document in result) {
                                        val comment = thoughtBox.find(Thought_.uuid, document.data["uuid"].toString()).firstOrNull()
                                        if (document.data["uuid"] in uuids && !comment?.isApproved!!) {
                                            docsref.document(document.id)
                                                    .update("isApproved", true).addOnSuccessListener {
                                                        val comment_body = HashMap<String, Any>()
                                                        comment_body["timestamp"] = comment.timestamp
                                                        comment_body["text"] = comment.textString
                                                        comment_body["uuid"] = comment.uuid
                                                        comment_body["actual_owner"] = comment.user.target.blockstackId
                                                        presenter.sendComment(user.blockstackId, uuid, comment_body)
                                                    }
                                            comment.isApproved = true
                                            thoughtBox.put(comment)
                                            adapter.update(comment)
                                            count += 1
                                        }
                                    }
                                    Toast.makeText(this, count.toString() + " comment/s made public", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Log.d(TAG, "Error getting documents: ", exception)
                                }
                    } else
                        Toast.makeText(this, "Select some private comments you want to make public", Toast.LENGTH_SHORT).show()
                    adapter.unselectAllItems()
                }
            }
        } else {
            close()
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
        App.mixpanel.track("Discuss");
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
