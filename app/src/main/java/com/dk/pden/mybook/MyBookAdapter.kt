package com.dk.pden.mybook

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dk.pden.R
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import com.dk.pden.mybook.holder.BookBaseViewHolder
import com.dk.pden.mybook.holder.BookInteractionListener
import com.dk.pden.mybook.holder.BookStatusViewHolder
import java.util.*

open class MyBookAdapter(val listener: BookInteractionListener) : RecyclerView.Adapter<BookBaseViewHolder>() {

    var thoughts: MutableList<Thought> = ArrayList()
    var user: User? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookBaseViewHolder =
            BookStatusViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.thought_basic, parent, false), listener)

    override fun onBindViewHolder(holderBook: BookBaseViewHolder, position: Int) {
        holderBook.setup(thoughts[position], this.user!!)
    }

    override fun getItemCount() = thoughts.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.thought_basic
    }

}