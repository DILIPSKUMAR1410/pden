package com.dk.pen.mybook

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.ProgressBar
import com.dk.pen.R


class MyBookActivity : AppCompatActivity() {


//    private lateinit var adapter: TimelineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_book)

        recyclerView = findViewById(R.id.tweetsRecyclerView) as RecyclerView
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        loadingProgressBar = findViewById(R.id.loadingProgressBar) as ProgressBar

    }
}
