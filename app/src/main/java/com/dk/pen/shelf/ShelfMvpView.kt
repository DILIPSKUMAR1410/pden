package com.dk.pen.shelf

import com.dk.pen.base.MvpView
import com.dk.pen.model.Thought

interface ShelfMvpView : MvpView {

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

}