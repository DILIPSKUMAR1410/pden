package com.dk.pden.service.ApiResponses

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class ApiResponse(
        @SerializedName("results") val users: List<JsonObject>)