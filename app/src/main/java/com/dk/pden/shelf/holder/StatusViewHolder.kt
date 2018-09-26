package com.dk.pden.shelf.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.view.View
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.model.Thought

open class StatusViewHolder(container: View) :
        BaseViewHolder(container) {


    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought) {

        val currentThought = thought
        val currentUser = thought.user.target
//        userNameTextView.text = currentUser.name
        userScreenNameTextView.text = "@${currentUser.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(currentThought.timestamp)}"
        userProfilePicImageView.loadAvatar(currentUser.avatarImage)
        statusTextView.text = currentThought.text
    }


}