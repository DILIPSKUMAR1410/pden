package com.dk.pen.model

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class User(val name: String)
{
        @Id
        var id: Long = 0

        @Backlink(to = "user")
        lateinit var thought: List<Thought>

        var description: String = ""

        var avatarImage: String = ""

        var blockstackId: String = ""


}
