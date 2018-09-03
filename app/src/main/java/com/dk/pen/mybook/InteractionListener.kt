package com.dk.pen.mybook

import com.dk.pen.model.Thought
import com.dk.pen.model.User

interface InteractionListener {

    fun openTweet(tweet: Thought)

    fun showUser(user: User)

}