package com.dk.pen.service.ApiResponses

import com.google.gson.annotations.SerializedName

data class PublishStatusApiResponse(
        @SerializedName("name") val status: String)