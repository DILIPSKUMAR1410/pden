package com.dk.pden.mybook

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.common.visible
import com.dk.pden.custom.decorators.SpaceTopItemDecoration
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.model.User_
import io.objectbox.Box


class MyBookActivity : AppCompatActivity(), MyBookMvpView {

    companion object {
        const val TAG_USER_blockstackId = "user_blockstackId"
        const val TAG_USER_avatarImage = "user_avatarImage"
        const val TAG_USER_description = "user_description"
        const val TAG_USER_name = "user_name"

        private var user: User? = null

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
    private lateinit var name: TextView
    private lateinit var blockstack_name: TextView
    private lateinit var about_me: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var toggleAddToShelf: ToggleButton
    private lateinit var appBar: AppBarLayout
    private var self: Boolean = false
    private fun getMyBookPresenter() = MyBookPresenter()
    private lateinit var blockstack_id: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_book)
        presenter.attachView(this)
        val preferencesHelper = PreferencesHelper(this)
        val my_blockstack_id = preferencesHelper.blockstackId
        adapter = MyBookAdapter()
        recyclerView = findViewById(R.id.tweetsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        toggleAddToShelf = findViewById(R.id.toggleAddToShelf)
        avatar = findViewById(R.id.avatar)
        tcountvalue = findViewById(R.id.tcountvalue)
        name = findViewById(R.id.name)
        blockstack_name = findViewById(R.id.blockstack_id)
        about_me = findViewById(R.id.about_me)

        blockstack_id = intent.getStringExtra(TAG_USER_blockstackId)

        userBox = ObjectBox.boxStore.boxFor(User::class.java)
        user = userBox.find(User_.blockstackId, blockstack_id).firstOrNull()

        if (user == null) {
            user = User(blockstack_id)
            user!!.avatarImage = intent.getStringExtra(TAG_USER_avatarImage)
            user!!.name = intent.getStringExtra(TAG_USER_name)
            user!!.description = intent.getStringExtra(TAG_USER_description)
        } else if (my_blockstack_id.equals(blockstack_id)) {
            self = true
            toggleAddToShelf.visibility = View.INVISIBLE
        } else if (user!!.isFollowed) {
            setBorrowed(true)
        }

        if (!self) {
            toggleAddToShelf.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    toggleAddToShelf.isEnabled = false
                    presenter.addInterest(this, user!!)
                } else {
                    toggleAddToShelf.isEnabled = false
                    presenter.removeInterest(this, user!!)
                }
            }
        }

        tcountvalue.text = user!!.thoughts.size.toString()
        name.text = if (user!!.name.isNotEmpty()) user!!.name else "-NA-"
        blockstack_name.text = user!!.blockstackId
        about_me.text = if (user!!.description.isNotEmpty()) user!!.description else "-NA-"
        avatar.loadAvatar(user!!.avatarImage)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpaceTopItemDecoration(Utils.dpToPx(this, 10)))
        recyclerView.adapter = adapter
        adapter.user = user

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

        swipeRefreshLayout.setOnRefreshListener {
            presenter.onRefresh(this, user!!, self)
        }

        if (adapter.thoughts.isEmpty()) {
            if (user!!.thoughts.isNotEmpty()) showThoughts(user!!.thoughts.asReversed() as MutableList<Thought>)
            else presenter.onRefresh(this, user!!, self)
        }
    }


    override fun showThoughts(thoughts: MutableList<Thought>) {

        runOnUiThread {
            adapter.thoughts = thoughts
            tcountvalue.text = adapter.thoughts.size.toString()
            adapter.notifyDataSetChanged()
        }
    }

    override fun showThought(thought: Thought) {
        if (adapter.thoughts.isNotEmpty()) {
            var removedPosition = 0
            removedPosition = adapter.thoughts.size - 1
            adapter.thoughts.removeAt(removedPosition)
            adapter.notifyItemRemoved(removedPosition)
        }
        adapter.thoughts.add(0, thought)
        tcountvalue.text = adapter.thoughts.size.toString()
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
    }

    override fun showMoreMyThoughts(thoughts: MutableList<Thought>) {
        adapter.thoughts.addAll(thoughts)
        tcountvalue.text = adapter.thoughts.size.toString()
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

    override fun setBorrowed(flag: Boolean) {
        runOnUiThread {
            toggleAddToShelf.isEnabled = true
            toggleAddToShelf.isChecked = flag
        }
    }
}
