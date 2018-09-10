package com.dk.pen.model

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.io.Serializable

@Entity
data class User(val blockstackId: String): Serializable {
    constructor() : this("")


    @Id
    var id: Long = 0

    @Backlink(to = "user")
    lateinit var thoughts: ToMany<Thought>

    var description: String = ""

    var avatarImage: String = ""

    var name: String = ""

}

