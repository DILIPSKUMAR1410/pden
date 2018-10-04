package com.dk.pden.shelf

import com.dk.pden.model.Thought
import com.dk.pden.model.User

interface InteractionListener {

    fun spread(thought: Thought)

    fun showUser(user: User)

}