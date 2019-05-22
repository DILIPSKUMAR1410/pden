package com.dk.pden.model

import com.beust.klaxon.Json
import com.stfalcon.chatkit.commons.models.IMessage
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne
import java.util.*





@Entity
data class Thought(@Json(name = "text")
                   var textString: String,
                   @Json(name = "timestamp")
                   var timestamp: Long) : IMessage {

    constructor() : this("", 0)

    @Json(ignored = true)
    var uuid: String = UUID.randomUUID().toString()

    @Id
    @Json(ignored = true)
    var id: Long = 0

    @Json(ignored = true)
    lateinit var user: ToOne<User>

    @Json(ignored = true)
    lateinit var spreadBy: ToOne<User>

    var isSpread: Boolean = false

    var isApproved: Boolean = false

    var isComment: Boolean = false

    lateinit var discussion: ToOne<Discussion>

    @Backlink(to = "thought")
    lateinit var transactions: ToMany<Transaction>

    override fun getId(): String {
        return id.toString()
    }

    override fun getText(): String {
        return textString
    }

    override fun getUser(): User {
        return user.target
    }

    override fun getCreatedAt(): Date {
        return Date(timestamp)
    }

}