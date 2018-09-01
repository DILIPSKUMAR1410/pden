package com.dk.pen.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id


@Entity
data class MyThought(val text: String, val timestamp: String)
{
    @Id
    val id: Long = 0



}