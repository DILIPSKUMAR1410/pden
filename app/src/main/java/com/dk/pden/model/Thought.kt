package com.dk.pden.model

import com.beust.klaxon.Json
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.*

@Entity
data class Thought(@Json(name = "text")
                   var text: String,
                   @Json(name = "timestamp")
                   var timestamp: Long) {

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

    var isComment: Boolean = false

    @Backlink(to = "discussion")
    lateinit var discussion: ToOne<Discussion>

}