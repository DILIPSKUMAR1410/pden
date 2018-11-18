package com.dk.pden.feed.holder

import com.dk.pden.model.Thought
import com.dk.pden.model.User

interface FeedInteractionListener {

    fun spread(thought: Thought)

    fun showUser(user: User)

    fun showThread(thought: Thought)

}