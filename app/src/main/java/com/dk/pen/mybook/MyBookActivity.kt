package com.dk.pen.mybook

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.dk.pen.R
import com.dk.pen.common.Utils
import com.dk.pen.common.visible
import com.dk.pen.custom.decorators.SpaceTopItemDecoration
import com.dk.pen.model.Thought
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import java.sql.Timestamp


class MyBookActivity : AppCompatActivity(),MyBookMvpView {

    private val presenter: MyBookPresenter by lazy {getMyBookPresenter()}
    private lateinit var adapter: MyBookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private fun getMyBookPresenter() = MyBookPresenter()
    private var _blockstackSession: BlockstackSession? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_book)
        presenter.attachView(this)
        adapter = MyBookAdapter()
        recyclerView = findViewById(R.id.tweetsRecyclerView) as RecyclerView
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        loadingProgressBar = findViewById(R.id.loadingProgressBar) as ProgressBar

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpaceTopItemDecoration(Utils.dpToPx(this, 10)))
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (linearLayoutManager.childCount + linearLayoutManager
                                .findFirstVisibleItemPosition() + 1 > linearLayoutManager.itemCount - 10)
//                    presenter.getMoreThoughts()
                Log.d("Need more thoughts-->>", "Fixthis")

            }
        })

//        swipeRefreshLayout.setOnRefreshListener { presenter.onRefresh() }

                    if (adapter.thoughts.isEmpty())
                    {
                        val options = GetFileOptions()
                        Utils.getblockstackSession(this).getFile("MyThoughts.json", options, { contentResult ->
                            if (contentResult.hasValue) {
                                val content = contentResult.value!!.toString()
                                Log.d("MyThoughts", "File contents: ${content}")
                            } else {
                                Toast.makeText(this, "error: " + contentResult.error, Toast.LENGTH_SHORT).show()
                            }
                        })

                    }



        var myList: MutableList<Thought> = mutableListOf<Thought>()
        var t = Thought("Hi test", Timestamp(System.currentTimeMillis()).time)
//        var u = User("dilip")
//        u.blockstackId = "dilipkumar.id.blockstack"
//        var userBox = ObjectBox.boxStore.boxFor(User::class.java)
//        userBox.put(u)
//        t.user.setAndPutTargetAlways(u)
        myList.add(t)
        showThoughts(myList)

    }


    override fun showThoughts(thoughts: MutableList<Thought>) {
        adapter.thoughts = thoughts
        adapter.notifyDataSetChanged()
    }

    override fun showThought(thought: Thought) {
        val removedPosition = adapter.thoughts.size - 1
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
        swipeRefreshLayout.isRefreshing = false
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
