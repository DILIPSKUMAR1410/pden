package com.dk.pden.shelf.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dk.pden.model.Thought
import com.dk.pden.shelf.InteractionListener
import kotlinx.android.synthetic.main.item_userinfo.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*

abstract class BaseViewHolder(val container: View, val listener: InteractionListener) :
        RecyclerView.ViewHolder(container) {

    protected val userNameTextView: TextView = container.userNameTextView
    protected val userScreenNameTextView: TextView = container.userScreenNameTextView
    protected val statusTextView: TextView = container.statusTextView
    protected val timeTextView: TextView = container.timeTextView
    protected val userProfilePicImageView: ImageView = container.userProfilePicImageView

    abstract fun setup(thought: Thought)

}