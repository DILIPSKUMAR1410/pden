package com.dk.pden.mybook

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dk.pden.R
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.mybook.holder.BaseViewHolder
import com.dk.pden.mybook.holder.StatusViewHolder
import java.util.*

open class MyBookAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var thoughts: MutableList<Thought> = ArrayList()
    var user: User? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            StatusViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.thought_basic, parent, false))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.setup(thoughts[position], this.user!!)
    }

    override fun getItemCount() = thoughts.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.thought_basic
    }

}