package com.dk.pden.feed

import com.dk.pden.base.MvpView
import com.dk.pden.model.Thought

interface ConversationMvpView : MvpView {

    fun showThoughts(thoughts: MutableList<Thought>)

    fun showThought(thought: Thought)

    fun showMoreMyThoughts(thoughts: MutableList<Thought>)

    fun getLastMyThoughtId(): Long

    fun stopRefresh()

    fun showLoading()

    fun hideLoading()

    fun updateRecyclerViewView()

    fun startRefresh()

    fun removeThoughts(thoughts: MutableList<Thought>)

    fun close()

    fun updateAdapter()

}