package com.dk.pden.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne


@Entity
data class Transaction(

        var from: String,

        var to: String,

        var amount: Int,

        var actvity: String
) {
    @Id
    var id: Long = 0
    var timestamp: Long = System.currentTimeMillis()
    lateinit var thought: ToOne<Thought>
}