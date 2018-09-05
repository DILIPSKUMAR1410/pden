package com.dk.pen.model

import com.beust.klaxon.Json
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne


@Entity
data class Thought(    @Json(name = "text")
                       var text: String,
                       @Json(name = "timestamp")
                       var timestamp: Long)

{

    constructor() : this("",0)


    @Id
    @Json(ignored = true)
    var id: Long = 0

    @Json(ignored = true)
    lateinit var user: ToOne<User>


}