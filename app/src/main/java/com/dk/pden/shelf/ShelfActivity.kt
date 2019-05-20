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
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.Utils
import com.dk.pden.common.visible
import com.dk.pden.custom.decorators.SpaceTopItemDecoration
import com.dk.pden.model.User
import com.dk.pden.model.User_
import io.objectbox.Box

class ShelfActivity : AppCompatActivity(), ShelfUsersMvpView {

    companion object {
        private val ARG_QUERY = "query"

        fun launch(context: Context) {
            val intent = Intent(context, ShelfActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var query: String
    private lateinit var adapterShelf: ShelfUsersAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userBox: Box<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        adapterShelf = ShelfUsersAdapter()
        mixpanel.timeEvent("Shelf");
//        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.elevation = 4.0F
//        toolbar.setNavigationOnClickListener { finish() }
        title = "Shelf"
        userBox = ObjectBox.boxStore.boxFor(User::class.java)

        recyclerView = findViewById(R.id.tweetsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.isEnabled = false

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpaceTopItemDecoration(Utils.dpToPx(this, 10)))
        recyclerView.adapter = adapterShelf
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        })

        if (adapterShelf.users.isEmpty()) {
            showUsers(userBox.query().equal(User_.isFollowed, true).build().find())
        }

    }

    override fun showUsers(users: MutableList<User>) {
        adapterShelf.users = users
        adapterShelf.notifyDataSetChanged()
    }

    override fun showMoreUsers(users: MutableList<User>) {
        adapterShelf.users.addAll(users)
    }

    override fun showLoading() {
        loadingProgressBar.visible()
    }

    override fun hideLoading() {
        loadingProgressBar.visible(false)
    }

    override fun updateRecyclerViewView() {
        adapterShelf.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        close()
        return true
    }

    override fun close() {
        mixpanel.track("Shelf")
        finish()
    }
}
