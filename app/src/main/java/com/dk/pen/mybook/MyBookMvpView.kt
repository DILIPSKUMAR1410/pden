package com.dk.pen.mybook

import com.dk.pen.base.MvpView
import com.dk.pen.model.Thought
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions


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

    fun getactivity(bs: BlockstackSession, options: GetFileOptions)

}