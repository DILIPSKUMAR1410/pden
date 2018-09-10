package com.dk.pen.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ProgressBar
import com.dk.pen.R
import com.dk.pen.common.Utils
import com.dk.pen.common.visible
import com.dk.pen.custom.decorators.SpaceTopItemDecoration
import com.dk.pen.model.User

class SearchActivity : AppCompatActivity(), SearchUsersMvpView {

    companion object {
        private val ARG_QUERY = "query"

        fun launch(context: Context, query: String) {
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra(ARG_QUERY, query)
            context.startActivity(intent)
        }
    }

    private lateinit var query: String
    private lateinit var adapter: UsersAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val presenter: SearchUsersPresenter by lazy {
        SearchUsersPresenter(query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        adapter = UsersAdapter()
        query = intent.getStringExtra(ARG_QUERY)
        presenter.attachView(this)

//        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener { finish() }
        title = query


        recyclerView = findViewById(R.id.tweetsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.isEnabled = false

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpaceTopItemDecoration(Utils.dpToPx(this, 10)))
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        })

        if (adapter.users.isEmpty()) {
            presenter.getUsers()
        }

    }

    override fun showUsers(users: MutableList<User>) {
        adapter.users = users
        adapter.notifyDataSetChanged()
    }

    override fun showMoreUsers(users: MutableList<User>) {
        adapter.users.addAll(users)
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

}
