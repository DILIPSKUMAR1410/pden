package com.dk.pden.feed.holder

import android.annotation.SuppressLint
import android.support.annotation.CallSuper
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.dk.pden.R
import com.dk.pden.base.holder.FeedBaseViewHolder
import com.dk.pden.common.Utils
import com.dk.pden.common.loadAvatar
import com.dk.pden.common.visible
import com.dk.pden.model.Thought
import kotlinx.android.synthetic.main.item_interaction.view.*
import kotlinx.android.synthetic.main.thought_basic.view.*


open class FeedStatusViewHolder(container: View, listener: FeedInteractionListener) :
        FeedBaseViewHolder(container, listener) {

    protected var spreadTextView: TextView = container.spreadTextView
    protected var loveImageButton: LottieAnimationView? = container.love
    protected var spreadImageButton: ImageButton = container.spreadImageButton
    protected var threadImageButton: ImageButton = container.threadImageButton
    protected var burnTextView: TextView = container.burnTextView
    protected var earnTextView: TextView = container.earnTextView
    protected var spreadOutsideImageButton: ImageButton = container.spreadOutsideImageButton
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
        if (thought.user.target.isSelf) {
            burnTextView.visible()
            earnTextView.visible()
            var burn = 0
            var earn = 0
            if (0 < thought.transactions.count()) {
                thought.transactions.filter { it.from == thought.user.target.blockstackId }
                        .forEach { burn = (burn + it.amount).toInt() }
                thought.transactions.filter { it.to == thought.user.target.blockstackId }
                        .forEach { earn = (earn + it.amount).toInt() }
            }

            burnTextView.text = if (burn != 0) burn.toString() else "FREE POST"
            earnTextView.text = earn.toString()
        }

        if (thought.isSpread) spreadImageButton.setImageResource(R.drawable.ic_repeat_blue)
        else if (!thought.user.target.isSelf) {
            spreadImageButton.setImageResource(R.drawable.ic_repeat)
            spreadImageButton.setOnClickListener {
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
        spreadOutsideImageButton.setImageResource(R.drawable.social_group
        )

//        userNameTextView.textString = currentUser.blockstackId
        userScreenNameTextView.text = "@${thought.user.target.blockstackId}"
        timeTextView.text = " • ${Utils.formatDate(thought.timestamp)}"
        userProfilePicImageView.loadAvatar(thought.user.target.avatarImage)



        threadImageButton.setImageResource(R.drawable.ic_thread)
        threadImageButton.setOnClickListener {
            listener.showThread(thought)
        }
        statusTextView.text = thought.textString

        spreadOutsideImageButton.setOnClickListener {
            listener.spreadOutside(thought)
        }
        userProfilePicImageView.setOnClickListener {
            listener.showUser(thought.user.target)
        }

        userScreenNameTextView.setOnClickListener {
            listener.showUser(thought.user.target)
        }

    }
}