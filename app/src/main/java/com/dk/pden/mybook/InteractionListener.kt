package com.dk.pden.mybook

import com.dk.pden.model.Thought
import com.dk.pden.model.User

interface InteractionListener {

    fun openTweet(tweet: Thought)

    fun showUser(user: User)

}