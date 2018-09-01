package com.dk.pen.model

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Interest(val name: String)
{
        @Id
        var id: Long = 0

        @Backlink(to = "interest")
        lateinit var thought: List<OthersThought>

        val description: String = ""

        val avatarImage: String = ""


}
