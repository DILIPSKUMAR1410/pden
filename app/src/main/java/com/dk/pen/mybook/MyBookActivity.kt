package com.dk.pen.mybook

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ProgressBar
import cafe.adriel.kbus.KBus
import com.dk.pen.ObjectBox
import com.dk.pen.R
import com.dk.pen.common.PreferencesHelper
import com.dk.pen.common.Utils
import com.dk.pen.common.visible
import com.dk.pen.compose.ComposeThoughtActivity
import com.dk.pen.custom.decorators.SpaceTopItemDecoration
import com.dk.pen.events.NewMyThoughtEvent
import com.dk.pen.model.Thought
import com.dk.pen.model.User
import com.dk.pen.model.User_
import io.objectbox.Box
import org.blockstack.android.sdk.BlockstackSession


class MyBookActivity : AppCompatActivity(),MyBookMvpView {

    private lateinit var thoughtBox: Box<Thought>
    private var _blockstackSession: BlockstackSession? = null
    private lateinit var userBox: Box<User>
    private val presenter: MyBookPresenter by lazy {getMyBookPresenter()}
    private lateinit var adapter: MyBookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var floatingActionButton: FloatingActionButton
    private fun getMyBookPresenter() = MyBookPresenter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_book)
        var context = this

        presenter.attachView(this)
        adapter = MyBookAdapter()
        recyclerView = findViewById(R.id.tweetsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        floatingActionButton = findViewById(R.id.fab_compose)

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


        swipeRefreshLayout.setOnRefreshListener { presenter.onRefresh(this) }

        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        Log.d("total thoughts-->>", thoughtBox.count().toString())

                if (adapter.thoughts.isEmpty())
                    {
                        // Get a instance of PreferencesHelper class
                        val preferencesHelper = PreferencesHelper(this)

                        // save token on preferences
                        val blockstack_id = preferencesHelper.deviceToken

                        userBox = ObjectBox.boxStore.boxFor(User::class.java)
                        val user = userBox.find(User_.blockstackId,blockstack_id).firstOrNull()

                        if (user != null) {
                            Log.d("thoughts-->>", user.thoughts.size.toString())
                            showThoughts(user.thoughts as MutableList<Thought>)
                        }
                    }

        floatingActionButton.setOnClickListener { view ->
            val intent = Intent(this, ComposeThoughtActivity::class.java)
            startActivity(intent)
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
        if (adapter.thoughts.isNotEmpty())
        {
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
