package com.dk.pden.conversation.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.model.Thought
import kotlinx.android.synthetic.main.item_interaction.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*


open class ConversationStatusViewHolder(container: View, listener: ConversationInteractionListener) :
        ConversationBaseViewHolder(container, listener) {

    protected var spreadTextView: TextView = container.spreadTextView
    protected var spreadImageButton: ImageButton = container.spreadImageButton

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought) {

//        userNameTextView.text = currentUser.blockstackId
        userScreenNameTextView.text = "@${thought.user.target.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(thought.timestamp)}"
        userProfilePicImageView.loadAvatar(thought.user.target.avatarImage)
        statusTextView.text = thought.text
        userProfilePicImageView.setOnClickListener {
            listener.showUser(thought.user.target)
        }


    }
}