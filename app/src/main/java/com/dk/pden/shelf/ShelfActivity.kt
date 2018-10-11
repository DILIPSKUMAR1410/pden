package com.dk.pden.shelf

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.SearchView
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils
import com.dk.pden.common.visible
import com.dk.pden.compose.ComposeThoughtActivity
import com.dk.pden.custom.decorators.SpaceTopItemDecoration
import com.dk.pden.events.NewThoughtsEvent
import com.dk.pden.events.RemoveThoughtsEvent
import com.dk.pden.model.Thought
import com.dk.pden.model.Thought_
import com.dk.pden.model.User
import com.dk.pden.model.User_
import com.dk.pden.mybook.MyBookActivity
import com.dk.pden.search.SearchActivity
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ShelfActivity : AppCompatActivity(), ShelfMvpView, InteractionListener {

    private val presenter: ShelfPresenter by lazy { getShelfPresenter() }
    private lateinit var adapter: ShelfAdapter
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private fun getShelfPresenter() = ShelfPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelf)
        mixpanel.timeEvent("Feed");

        adapter = ShelfAdapter(this)
        recyclerView = findViewById(R.id.tweetsRecyclerView)
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        floatingActionButton = findViewById(R.id.fab_compose)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        floatingActionButton.setOnClickListener { _ ->
            val intent = Intent(this, ComposeThoughtActivity::class.java)
            startActivity(intent)
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

        if (adapter.thoughts.isEmpty()) {
            showThoughts(thoughtBox.query()
                    .order(Thought_.timestamp, QueryBuilder.DESCENDING).build().find())
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.menu_shelf, menu)

        val search = menu?.findItem(R.id.action_search)
        val searchView = search?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                search(query)
                return true
            }

            override fun onQueryTextChange(s: String) = false
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.myBook -> openProfile()
        }

        return super.onOptionsItemSelected(item)
    }


    fun search(string: String) {
        SearchActivity.launch(this, string)
    }

    fun openProfile() {
        val preferencesHelper = PreferencesHelper(this)
        val blockstack_id = preferencesHelper.blockstackId
        val user = ObjectBox.boxStore.boxFor(User::class.java).find(User_.blockstackId, blockstack_id).firstOrNull()
        user?.let { MyBookActivity.launch(this, it) }
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


    override fun updateRecyclerViewView() {
        adapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddEvent(event: NewThoughtsEvent) {
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
        mixpanel.track("Feed");
    }


    override fun spread(thought: Thought) {
        presenter.spreadThought(thought, this)
    }


    override fun showUser(user: User) {
        MyBookActivity.launch(this, user)
    }

}
