package com.dk.pden.mybook.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.dk.pden.R
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import kotlinx.android.synthetic.main.item_interaction.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*

open class BookStatusViewHolder(container: View, listener: BookInteractionListener) :
        BookBaseViewHolder(container, listener) {
    protected var spreadTextView: TextView = container.spreadTextView
    protected var threadImageButton: ImageButton = container.threadImageButton
    protected var spreadImageButton: ImageButton = container.spreadImageButton

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought, user: User) {

        val currentThought = thought
        val currentUser = user
//        userNameTextView.textString = currentUser.blockstackId
        userScreenNameTextView.text = "@${currentUser.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(currentThought.timestamp)}"
        userProfilePicImageView.loadAvatar(currentUser.avatarImage)

        if (thought.isSpread) spreadImageButton.setImageResource(R.drawable.ic_repeat_blue)
        else if (!thought.user.target.isSelf) spreadImageButton.setImageResource(R.drawable.ic_repeat)

        statusTextView.text = currentThought.textString

        threadImageButton.setImageResource(R.drawable.ic_thread)
        threadImageButton.setOnClickListener {
            listener.showThread(thought)
        }

        spreadImageButton.setOnClickListener {
            if (!thought.isSpread) {
                if (!thought.isSpread) {
                    AlertDialog.Builder(container.context)
                            .setTitle(R.string.spread_title)
                            .setPositiveButton(R.string.spread)
                            { _, _ ->
                                spreadImageButton.setImageResource(R.drawable.ic_repeat_blue)
                                listener.spread(thought)
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .create().show()
                }
            }
        }

    }


}