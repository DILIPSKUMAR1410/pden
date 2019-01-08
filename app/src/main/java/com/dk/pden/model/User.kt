package com.dk.pden.model

import com.stfalcon.chatkit.commons.models.IUser
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.io.Serializable

@Entity
data class User(val blockstackId: String) : Serializable, IUser {
    override fun getAvatar(): String {
        return avatarImage
    }

    override fun getName(): String {
        return nameString
    }

    override fun getId(): String {
        return blockstackId
    }

    constructor() : this("")


    @Id
    var pk: Long = 0

    @Backlink(to = "user")
    lateinit var thoughts: ToMany<Thought>

    @Backlink(to = "spreadBy")
    lateinit var spreaded_thoughts: ToMany<Thought>

    var description: String = ""

    var email: String = ""

    var avatarImage: String = ""

    var nameString: String = ""

    var isSelf: Boolean = false

    var isFollowed: Boolean = false

}

