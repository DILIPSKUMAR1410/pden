package com.dk.pden.mybook.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.view.View
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.model.Thought
import com.dk.pden.model.User

open class StatusViewHolder(container: View) :
        BaseViewHolder(container) {


    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought, user: User) {

        val currentThought = thought
        val currentUser = user
//        userNameTextView.text = currentUser.blockstackId
        userScreenNameTextView.text = "@${currentUser.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(currentThought.timestamp)}"
        userProfilePicImageView.loadAvatar(currentUser.avatarImage)

        statusTextView.text = currentThought.text
    }


}