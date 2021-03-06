package com.dk.pden.feed

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dk.pden.R
import com.dk.pden.base.holder.FeedBaseViewHolder
import com.dk.pden.feed.holder.FeedInteractionListener
import com.dk.pden.feed.holder.FeedStatusViewHolder
import com.dk.pden.model.Thought
import java.util.*


open class FeedAdapter(val listener: FeedInteractionListener) : RecyclerView.Adapter<FeedBaseViewHolder>() {

    var thoughts: MutableList<Thought> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedBaseViewHolder =
            FeedStatusViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.thought_basic, parent, false), listener)

    override fun onBindViewHolder(holderFeed: FeedBaseViewHolder, position: Int) {
        holderFeed.setup(thoughts[position])
    }

    override fun getItemCount() = thoughts.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.thought_basic
    }
}