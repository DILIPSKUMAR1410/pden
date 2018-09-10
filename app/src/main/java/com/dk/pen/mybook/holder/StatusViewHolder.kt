package com.dk.pen.mybook.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.view.View
import com.dk.pen.common.Utils
import com.dk.pen.common.loadAvatar
import com.dk.pen.model.Thought

open class StatusViewHolder(container: View) :
        BaseViewHolder(container) {


    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought) {

        val currentThought = thought
        val currentUser = currentThought.user.target
        userNameTextView.text = currentUser.blockstackId
        userScreenNameTextView.text = "@${currentUser.name}"
        timeTextView.text = " • ${Utils.formatDate(currentThought.timestamp)}"
        userProfilePicImageView.loadAvatar(currentUser.avatarImage)

        statusTextView.text = currentThought.text
    }


}