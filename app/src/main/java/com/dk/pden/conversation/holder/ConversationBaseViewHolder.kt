package com.dk.pden.conversation.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dk.pden.model.Thought
import kotlinx.android.synthetic.main.item_userinfo.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*

abstract class ConversationBaseViewHolder(val container: View, val listener: ConversationInteractionListener) :
        RecyclerView.ViewHolder(container) {

    protected val userNameTextView: TextView = container.userNameTextView
    protected val userScreenNameTextView: TextView = container.userScreenNameTextView
    protected val statusTextView: TextView = container.statusTextView
    protected val timeTextView: TextView = container.timeTextView
    protected val userProfilePicImageView: ImageView = container.userProfilePicImageView

    abstract fun setup(thought: Thought)

}