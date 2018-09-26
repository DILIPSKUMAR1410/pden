package com.dk.pden.shelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dk.pden.R
import com.dk.pden.model.Thought
import com.dk.pden.shelf.holder.BaseViewHolder
import com.dk.pden.shelf.holder.StatusViewHolder
import java.util.*


open class ShelfAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var thoughts: MutableList<Thought> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            StatusViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.thought_basic, parent, false))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.setup(thoughts[position])
    }

    override fun getItemCount() = thoughts.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.thought_basic
    }

}