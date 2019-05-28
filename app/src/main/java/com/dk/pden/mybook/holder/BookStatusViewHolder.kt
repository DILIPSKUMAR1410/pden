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
import com.dk.pden.common.visible
import com.dk.pden.model.Thought
import com.dk.pden.model.User
import kotlinx.android.synthetic.main.item_interaction.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*

open class BookStatusViewHolder(container: View, listener: BookInteractionListener) :
        BookBaseViewHolder(container, listener) {
    protected var threadImageButton: ImageButton = container.threadImageButton
    protected var spreadImageButton: ImageButton = container.spreadImageButton
    protected var burnTextView: TextView = container.burnTextView
    protected var earnTextView: TextView = container.earnTextView

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought, user: User) {
        //        userNameTextView.textString = currentUser.blockstackId
        userScreenNameTextView.text = "@${user.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(thought.timestamp)}"
        userProfilePicImageView.loadAvatar(user.avatarImage)
        burnTextView.visible()
        earnTextView.visible()
        var burn = 0
        var earn = 0
        try {
            if (!thought.transactions.isNullOrEmpty()) {
                thought.transactions.filter { it.from == thought.user.target.blockstackId }
                        .forEach { burn = +it.amount.toInt() }
                thought.transactions.filter { it.to == thought.user.target.blockstackId }
                        .forEach { earn = +it.amount.toInt() }
            }
        } catch (e: IllegalStateException) {
            // Must be safe
        }


        burnTextView.text = if (burn != 0) burn.toString() else "FREE POST"
        earnTextView.text = earn.toString()
        if (thought.isSpread) spreadImageButton.setImageResource(R.drawable.ic_repeat_blue)
        else if (!thought.user.target.isSelf) spreadImageButton.setImageResource(R.drawable.ic_repeat)

        statusTextView.text = thought.textString

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