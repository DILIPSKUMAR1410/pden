package com.dk.pden.shelf.holder

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
import com.dk.pden.shelf.InteractionListener
import kotlinx.android.synthetic.main.item_interaction.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*


open class StatusViewHolder(container: View, listener: InteractionListener) :
        BaseViewHolder(container, listener) {

    protected var spreadTextView: TextView = container.spreadTextView
    protected var spreadImageButton: ImageButton = container.spreadImageButton

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought) {

        if (thought.spreadBy.isNull) {
            spreadTextView.visible(false)
        } else {
            spreadTextView.visible()
            spreadTextView.text = container.context.getString(
                    R.string.spreadDiscp)
        }

        if (thought.isSpread) spreadImageButton.setImageResource(R.drawable.ic_repeat_blue)
        else if (!thought.user.target.isSelf) spreadImageButton.setImageResource(R.drawable.ic_repeat)


//        userNameTextView.text = currentUser.blockstackId
        userScreenNameTextView.text = "@${thought.user.target.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(thought.timestamp)}"
        userProfilePicImageView.loadAvatar(thought.user.target.avatarImage)

        statusTextView.text = thought.text


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

        userProfilePicImageView.setOnClickListener {
            listener.showUser(thought.user.target)
        }


    }
}