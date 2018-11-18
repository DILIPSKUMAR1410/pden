package com.dk.pden.model

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.io.Serializable

@Entity
data class User(val blockstackId: String) : Serializable {
    constructor() : this("")


    @Id
    var id: Long = 0

    @Backlink(to = "user")
    lateinit var thoughts: ToMany<Thought>

    @Backlink(to = "spreadBy")
    lateinit var spreaded_thoughts: ToMany<Thought>

    var description: String = ""

    var avatarImage: String = ""

    var name: String = ""

    var isSelf: Boolean = false

    var isFollowed: Boolean = false

}
