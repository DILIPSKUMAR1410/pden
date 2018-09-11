package com.dk.pen.mybook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import cafe.adriel.kbus.KBus
import com.dk.pen.ObjectBox
import com.dk.pen.R
import com.dk.pen.common.Utils
import com.dk.pen.common.loadAvatar
import com.dk.pen.common.visible
import com.dk.pen.compose.ComposeThoughtActivity
import com.dk.pen.custom.decorators.SpaceTopItemDecoration
import com.dk.pen.events.NewMyThoughtEvent
import com.dk.pen.model.Thought
import com.dk.pen.model.User
import com.dk.pen.model.User_
import io.objectbox.Box


class MyBookActivity : AppCompatActivity(), MyBookMvpView {

    companion object {
        private val ARG_QUERY = "query"
        const val TAG_USER_blockstackId = "user_blockstackId"
        const val TAG_USER_avatarImage = "user_avatarImage"
        const val TAG_USER_description = "user_description"
        const val TAG_USER_name = "user_name"

        private var user: User? = null


        fun launch(context: Context, query: String) {
            val intent = Intent(context, MyBookActivity::class.java)
            intent.putExtra(ARG_QUERY, query)
            context.startActivity(intent)
        }

        fun launch(context: Context, user: User) {
            val intent = Intent(context, MyBookActivity::class.java)
            intent.putExtra(TAG_USER_blockstackId, user.blockstackId)
            intent.putExtra(TAG_USER_description, user.description)
            intent.putExtra(TAG_USER_avatarImage, user.avatarImage)
            intent.putExtra(TAG_USER_name, user.name)
            context.startActivity(intent)
        }
    }

    private lateinit var userBox: Box<User>
    private val presenter: MyBookPresenter by lazy { getMyBookPresenter() }
    private lateinit var adapter: MyBookAdapter
    private lateinit var avatar: ImageView
    private lateinit var tcountvalue: TextView
    private lateinit var icountvalue: TextView
    private lateinit var name: TextView
    private lateinit var blockstack_name: TextView
    private lateinit var about_me: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var toggleAddToShelf: ToggleButton


    private fun getMyBookPresenter() = MyBookPresenter()
    private lateinit var blockstack_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_book)

        presenter.attachView(this)
        adapter = MyBookAdapter()
        recyclerView = findViewById(R.id.tweetsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        floatingActionButton = findViewById(R.id.fab_compose)
        toggleAddToShelf = findViewById(R.id.toggleAddToShelf)
        avatar = findViewById(R.id.avatar)
        tcountvalue = findViewById(R.id.tcountvalue)
        icountvalue = findViewById(R.id.icountvalue)
        name = findViewById(R.id.name)
        blockstack_name = findViewById(R.id.blockstack_id)
        about_me = findViewById(R.id.about_me)

        if (intent.hasExtra(ARG_QUERY)) {
            blockstack_id = intent.getStringExtra(ARG_QUERY)
            userBox = ObjectBox.boxStore.boxFor(User::class.java)
            user = userBox.find(User_.blockstackId, blockstack_id).firstOrNull()
            toggleAddToShelf.setVisibility(View.INVISIBLE)
            floatingActionButton.setOnClickListener { view ->
                val intent = Intent(this, ComposeThoughtActivity::class.java)
                startActivity(intent)
            }

        } else {
            user = User(intent.getStringExtra(TAG_USER_blockstackId))
            user!!.avatarImage = intent.getStringExtra(TAG_USER_avatarImage)
            user!!.name = intent.getStringExtra(TAG_USER_name)
            user!!.description = intent.getStringExtra(TAG_USER_description)
            floatingActionButton.hide()
        }
        tcountvalue.text = user!!.thoughts.size.toString()
        icountvalue.text =  "-NA-"
        name.text = if (user!!.name.isNotEmpty()) user!!.name else "-NA-"
        blockstack_name.text = user!!.blockstackId
        about_me.text = if (user!!.description.isNotEmpty()) user!!.description else "-NA-"
        avatar.loadAvatar(user!!.avatarImage)


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


        swipeRefreshLayout.setOnRefreshListener { presenter.onRefresh(this, blockstack_id) }

        if (adapter.thoughts.isEmpty()) {
            if (user != null) {
                showThoughts(user!!.thoughts as MutableList<Thought>)
            }
        }

    }


    override fun showThoughts(thoughts: MutableList<Thought>) {

        runOnUiThread {
            adapter.thoughts = thoughts
            adapter.notifyDataSetChanged()
        }
    }

    override fun showThought(thought: Thought) {
        var removedPosition = 0
        if (adapter.thoughts.isNotEmpty()) {
            removedPosition = adapter.thoughts.size - 1
        }
        adapter.thoughts.removeAt(removedPosition)
        adapter.notifyItemRemoved(removedPosition)

        adapter.thoughts.add(0, thought)
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
    }

    override fun showMoreMyThoughts(thoughts: MutableList<Thought>) {
        adapter.thoughts.addAll(thoughts)
    }

    override fun getLastMyThoughtId(): Long = if (adapter.thoughts.size > 0) adapter.thoughts[0].id else -1

    override fun stopRefresh() {
        runOnUiThread {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun showLoading() {
        loadingProgressBar.visible()
    }

    override fun hideLoading() {
        loadingProgressBar.visible(false)
    }


    override fun updateRecyclerViewView() {
        adapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        KBus.subscribe<NewMyThoughtEvent>(this) {
            showThought(it.thought)
        }


    }

    override fun onStop() {
        super.onStop()
        KBus.unsubscribe(this)
    }


}
