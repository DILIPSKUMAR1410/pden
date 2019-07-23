package com.dk.pden.mybook.holder

import com.dk.pden.model.Thought

interface BookInteractionListener {

    fun spread(thought: Thought)

    fun showThread(thought: Thought)

    fun spreadOutside(thought: Thought)

}