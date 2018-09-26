package com.dk.pden.service

import com.dk.pden.service.ApiResponses.PublishStatusApiResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FirebaseService {

    @Headers(
            "Content-Type: application/json",
            "Authorization: key=AIzaSyDdce1-B_i-BgzFWgZzkd1wRDK9p1U_OdU"
    )
    @POST("fcm/send")
    fun publishToTopic(@Body msg: JsonObject): Single<PublishStatusApiResponse>
}
