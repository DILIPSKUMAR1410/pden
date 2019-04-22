package com.dk.pden.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.widget.ProgressBar
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.R
import com.dk.pden.common.visible
import com.dk.pden.model.User

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
    private lateinit var adapterSearch: SearchUsersAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val presenter: SearchUsersPresenter by lazy {
        SearchUsersPresenter(query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        adapterSearch = SearchUsersAdapter()
        query = intent.getStringExtra(ARG_QUERY)
        presenter.attachView(this)
        mixpanel.timeEvent("Search");
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.elevation = 4.0F
        title = query


        recyclerView = findViewById(R.id.tweetsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.isEnabled = false

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.adapter = adapterSearch
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        })

        if (adapterSearch.users.isEmpty()) {
            presenter.getUsers()
        }

    }

    override fun showUsers(users: MutableList<User>) {
        adapterSearch.users = users
        adapterSearch.notifyDataSetChanged()
    }

    override fun showMoreUsers(users: MutableList<User>) {
        adapterSearch.users.addAll(users)
    }

    override fun showLoading() {
        loadingProgressBar.visible()
    }

    override fun hideLoading() {
        loadingProgressBar.visible(false)
    }

    override fun updateRecyclerViewView() {
        adapterSearch.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        close()
        return true
    }

    override fun close() {
        mixpanel.track("Search")
        finish()
    }
}
