package com.dk.pen.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne


@Entity
data class OthersThought(val text: String, val interest: ToOne<Interest>, val timestamp: String)
{
    @Id
    val id: Long = 0
}