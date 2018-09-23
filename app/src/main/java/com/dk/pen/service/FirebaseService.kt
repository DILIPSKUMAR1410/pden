package com.dk.pen.service

import com.dk.pen.service.ApiResponses.PublishStatusApiResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FirebaseService {

    @Headers(
            "Content-Type: application/json",
            "Authorization: key=AIzaSyCKtADOFB1ShFfTkIXm4_0SFP_Li3h9gck"
    )
    @POST("fcm/send")
    fun publishToTopic(@Body msg: JsonObject): Single<PublishStatusApiResponse>
}
