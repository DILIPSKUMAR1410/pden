package com.dk.pden.discuss

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dk.pden.R
import com.dk.pden.discuss.holder.DiscussBaseViewHolder
import com.dk.pden.discuss.holder.DiscussInteractionListener
import com.dk.pden.discuss.holder.DiscussStatusViewHolder
import com.dk.pden.model.Thought
import java.util.*


open class DiscussAdapter(val listener: DiscussInteractionListener) : RecyclerView.Adapter<DiscussBaseViewHolder>() {

    var thoughts: MutableList<Thought> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussBaseViewHolder =
            DiscussStatusViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.thought_basic, parent, false), listener)

    override fun onBindViewHolder(holderDiscuss: DiscussBaseViewHolder, position: Int) {
        holderDiscuss.setup(thoughts[position])
    }

    override fun getItemCount() = thoughts.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.thought_basic
    }
}