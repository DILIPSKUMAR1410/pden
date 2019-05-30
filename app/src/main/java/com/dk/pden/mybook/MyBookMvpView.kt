package com.dk.pden.mybook

import com.dk.pden.base.MvpView
import com.dk.pden.model.Thought


/**
 * Created by andrea on 17/05/16.
 */
interface MyBookMvpView : MvpView {

    fun showThoughts(thoughts: MutableList<Thought>)

    fun showThought(thought: Thought)

    fun showMoreMyThoughts(thoughts: MutableList<Thought>)

    fun getLastMyThoughtId(): Long

    fun stopRefresh()

    fun showLoading()

    fun hideLoading()

    fun updateRecyclerViewView()

    fun startRefresh()

    fun setBorrowed(flag: Boolean)

    fun updateAdapter()

}