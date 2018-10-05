package com.dk.pden.mybook.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.view.View
import android.widget.ImageButton
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.common.visible
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import kotlinx.android.synthetic.main.item_interaction.view.*

open class StatusViewHolder(container: View) :
        BaseViewHolder(container) {

    protected var spreadImageButton: ImageButton = container.spreadImageButton

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought, user: User) {

        val currentThought = thought
        val currentUser = user
//        userNameTextView.text = currentUser.blockstackId
        userScreenNameTextView.text = "@${currentUser.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(currentThought.timestamp)}"
        userProfilePicImageView.loadAvatar(currentUser.avatarImage)
        spreadImageButton.visible(false)
        statusTextView.text = currentThought.text
    }


}