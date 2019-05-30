package com.dk.pden.mybook.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.dk.pden.App
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
    private var threadImageButton: ImageButton = container.threadImageButton
    private var spreadImageButton: ImageButton = container.spreadImageButton
    private var burnTextView: TextView = container.burnTextView
    private var earnTextView: TextView = container.earnTextView
    private var loveImageButton: LottieAnimationView? = container.love
    private var spreadOutsideImageButton: ImageButton = container.spreadOutsideImageButton


    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun setup(thought: Thought, user: User) {
        //        userNameTextView.textString = currentUser.blockstackId
        userScreenNameTextView.text = "@${user.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(thought.timestamp)}"
        userProfilePicImageView.loadAvatar(user.avatarImage)


        if (thought.user.target.isSelf) {
            burnTextView.visible()
            if (!thought.transactions.isNullOrEmpty()) {
                var burn = 0
                var earn = 0
                thought.transactions.filter { it.from == App.BLOCKSTACK_ID }
                        .forEach { burn = +it.amount.toInt() }
                thought.transactions.filter { it.to == App.BLOCKSTACK_ID }
                        .forEach { earn = +it.amount.toInt() }
                earnTextView.visible()
                burnTextView.text = if (burn != 0) burn.toString() else "FREE POST"
                earnTextView.text = if (earn != 0) earn.toString() else null
            } else
                burnTextView.text = "FREE POST"
        } else {
            loveImageButton?.visible()
            if (thought.isLoved) loveImageButton?.progress = 1f
            else {
                loveImageButton!!.setOnClickListener {
                    if (!thought.isLoved) {
                        loveImageButton!!.playAnimation();
                        listener.love(thought)
                    }
                }
            }
            if (!thought.transactions.isNullOrEmpty()) {
                var burn = 0
                thought.transactions.filter { it.from == App.BLOCKSTACK_ID }
                        .forEach { burn = +it.amount.toInt() }
                burnTextView.text = if (burn != 0) burn.toString() else null
            }
        }


        if (thought.isSpread) spreadImageButton.setImageResource(R.drawable.ic_repeat_blue)
        else if (!thought.user.target.isSelf) spreadImageButton.setImageResource(R.drawable.ic_repeat)

        statusTextView.text = thought.textString

        threadImageButton.setImageResource(R.drawable.ic_thread)
        threadImageButton.setOnClickListener {
            listener.showThread(thought)
        }
        
        spreadOutsideImageButton.setImageResource(R.drawable.social_group)
        spreadOutsideImageButton.setOnClickListener {
            listener.spreadOutside(thought)
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