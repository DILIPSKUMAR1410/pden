package com.dk.pden.conversation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.Utils
import com.dk.pden.common.visible
import com.dk.pden.compose.ComposeThoughtActivity
import com.dk.pden.conversation.holder.ConversationInteractionListener
import com.dk.pden.custom.decorators.SpaceTopItemDecoration
import com.dk.pden.events.NewCommentEvent
import com.dk.pden.events.RemoveThoughtsEvent
import com.dk.pden.feed.ConversationMvpView
import com.dk.pden.feed.ConversationPresenter
import com.dk.pden.model.*
import com.dk.pden.mybook.MyBookActivity
import io.objectbox.Box
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ConversationActivity : AppCompatActivity(), ConversationMvpView, ConversationInteractionListener {

    private val presenter: ConversationPresenter by lazy { getShelfPresenter() }
    private lateinit var adapter: ConversationAdapter
    private lateinit var conversationBox: Box<Conversation>
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private fun getShelfPresenter() = ConversationPresenter()


    companion object {

        private var uuid: String = ""
        fun launch(context: Context, uuid: String) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra("uuid", uuid)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        // Get the support action bar
        val actionBar = supportActionBar

        // Set the action bar title, subtitle and elevation
        actionBar!!.title = "Conversation"
        actionBar.elevation = 4.0F
        actionBar.setDisplayHomeAsUpEnabled(true)

        mixpanel.timeEvent("Conversation");
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        conversationBox = ObjectBox.boxStore.boxFor(Conversation::class.java)

//        threadBox = ObjectBox.boxStore.boxFor(Conversation::class.java)
        adapter = ConversationAdapter(this)
        uuid = intent.getStringExtra("uuid")
        recyclerView = findViewById(R.id.tweetsRecyclerView)
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        floatingActionButton = findViewById(R.id.fab_compose)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        floatingActionButton.setOnClickListener { _ ->
            ComposeThoughtActivity.launch(this, uuid)
        }
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpaceTopItemDecoration(Utils.dpToPx(this, 10)))
        recyclerView.adapter = adapter

//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                if (linearLayoutManager.childCount + linearLayoutManager
//                                .findFirstVisibleItemPosition() + 1 > linearLayoutManager.itemCount - 10)
//                    presenter.getMoreThoughts(this)
//                Log.d("Need more thoughts-->>", "Fixthis")
//
//            }
//        })


//        swipeRefreshLayout.setOnRefreshListener {
//            //            presenter.onRefresh(this, MyBookActivity.user!!, self)
//        }
        val conversation = conversationBox.find(Conversation_.uuid, uuid).firstOrNull()
        if (conversation == null) {
            showThoughts(thoughtBox.find(Thought_.uuid, uuid))
        } else
            showThoughts(conversation.thoughts.asReversed())


    }

    override fun showThoughts(thoughts: MutableList<Thought>) {

        runOnUiThread {
            adapter.thoughts = thoughts
            adapter.notifyDataSetChanged()
        }
    }

    override fun updateAdapter() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    override fun removeThoughts(thoughts: MutableList<Thought>) {
        runOnUiThread {
            adapter.thoughts.removeAll(thoughts)
            adapter.notifyDataSetChanged()
        }
    }

    override fun showThought(thought: Thought) {
        var removedPosition = adapter.thoughts.size - 1
        if (removedPosition < 0) {
            removedPosition = 0
            adapter.thoughts.removeAt(removedPosition)
            adapter.notifyItemRemoved(removedPosition)
        }

        adapter.thoughts.add(0, thought)
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
    }

    override fun showMoreMyThoughts(thoughts: MutableList<Thought>) {
        thoughts.addAll(adapter.thoughts)
        thoughts.sortByDescending { it.timestamp }
        adapter.thoughts = thoughts
        adapter.notifyDataSetChanged()
    }

    override fun getLastMyThoughtId(): Long = if (adapter.thoughts.size > 0) adapter.thoughts[0].id else -1

    override fun stopRefresh() {
        runOnUiThread {
            if (swipeRefreshLayout.isRefreshing)
                swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun startRefresh() {
        runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }
    }

    override fun showLoading() {
        if (!swipeRefreshLayout.isRefreshing)
            loadingProgressBar.visible()
    }

    override fun hideLoading() {
        if (loadingProgressBar.isEnabled)
            loadingProgressBar.visible(false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        close()
        return true
    }

    override fun updateRecyclerViewView() {
        adapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddEvent(event: NewCommentEvent) {
        /* Do something */
        if (event.thoughts.size > 1)
            showMoreMyThoughts(event.thoughts)
        else
            showThought(event.thoughts[0])
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveEvent(event: RemoveThoughtsEvent) {
        /* Do something */
        removeThoughts(event.thoughts)

    }

    public override fun onStart() {
        super.onStart()
        Log.d("Eventbus -->>", "regiser")
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)

    }

    public override fun onStop() {
        super.onStop()
        mixpanel.track("Conversation");
    }

    override fun showUser(user: User) {
        MyBookActivity.launch(this, user)
    }

    override fun close() {
        finish()
    }
}
