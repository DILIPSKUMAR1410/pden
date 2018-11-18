package com.dk.pden.conversation

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dk.pden.R
import com.dk.pden.conversation.holder.ConversationBaseViewHolder
import com.dk.pden.conversation.holder.ConversationInteractionListener
import com.dk.pden.conversation.holder.ConversationStatusViewHolder
import com.dk.pden.model.Thought
import java.util.*


open class ConversationAdapter(val listener: ConversationInteractionListener) : RecyclerView.Adapter<ConversationBaseViewHolder>() {

    var thoughts: MutableList<Thought> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationBaseViewHolder =
            ConversationStatusViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.thought_basic, parent, false), listener)

    override fun onBindViewHolder(holderConversation: ConversationBaseViewHolder, position: Int) {
        holderConversation.setup(thoughts[position])
    }

    override fun getItemCount() = thoughts.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.thought_basic
    }
}