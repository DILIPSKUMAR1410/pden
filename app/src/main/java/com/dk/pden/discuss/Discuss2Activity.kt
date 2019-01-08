package com.dk.pden.discuss

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.loadAvatar
import com.dk.pden.compose.ComposeThoughtPresenter
import com.dk.pden.discuss.holder.CustomIncomingTextMessageViewHolder
import com.dk.pden.events.NewCommentEvent
import com.dk.pden.model.*
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
import org.json.JSONObject




class Discuss2Activity : AppCompatActivity(), MessageInput.InputListener,
        MessageInput.TypingListener {
    private val presenter: ComposeThoughtPresenter by lazy { ComposeThoughtPresenter() }
    private lateinit var discussionBox: Box<Discussion>
    private lateinit var userBox: Box<User>
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var adapter: MessagesListAdapter<Thought>
    private lateinit var user: User
    private lateinit var imageLoader: ImageLoader
    private var blockstack_id = ""


    companion object {

        private var uuid: String = ""
        fun launch(context: Context, uuid: String) {
            val intent = Intent(context, Discuss2Activity::class.java)
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

        messagesList.setAdapter(adapter)
        uuid = intent.getStringExtra("uuid")
        val conversation = discussionBox.find(Discussion_.uuid, uuid).firstOrNull()
        if (conversation == null) {
            adapter.addToEnd(thoughtBox.find(Thought_.uuid, uuid), true)
        } else
            adapter.addToEnd(conversation.thoughts, true)

        input.setInputListener(this);
        input.setTypingListener(this);
    }


    override fun onSubmit(input: CharSequence): Boolean {

        val rootObject = JSONObject()
//        val props = JSONObject()
        val thought = Thought(input.toString(), System.currentTimeMillis())
        thought.isComment = true
        rootObject.put("timestamp", thought.timestamp)
        rootObject.put("text", thought.textString)
        rootObject.put("uuid", thought.uuid)
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
        rootObject.put("actual_owner", blockstack_id)
        adapter.addToStart(thought, true)
        presenter.sendThought(uuid, rootObject)
//        val mutableList: MutableList<Thought> = ArrayList()
//        mutableList.add(thought)
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
