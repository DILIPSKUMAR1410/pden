package com.dk.pden.model

import com.beust.klaxon.Json
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany


@Entity
data class Conversation(@Json(ignored = true)
                        var uuid: String) {

    constructor() : this("")

    @Id
    @Json(ignored = true)
    var id: Long = 0

    @Json(ignored = true)
    lateinit var thoughts: ToMany<Thought>

}