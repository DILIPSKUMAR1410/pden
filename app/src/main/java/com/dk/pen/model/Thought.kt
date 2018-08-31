package com.dk.pen.model

import java.io.Serializable

data class Thought(val status: String) : Serializable {

    val text: String = status
//    val id: Long = status.id
//    val timeStamp: Long =
//    val user: User = status.user


}