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
            "Authorization: Bearer AF3FF3FB2924281983BAB4D2DA02E5DDB8AA505CC2D60E953ABAB70B02AB5593"
    )
    @POST("interests")
    fun publishToTopic(@Body msg: JsonObject): Single<PublishStatusApiResponse>

    @Headers(
            "Content-Type: application/json",
            "Authorization: Bearer AF3FF3FB2924281983BAB4D2DA02E5DDB8AA505CC2D60E953ABAB70B02AB5593"
    )
    @POST("users")
    fun publishToUser(@Body msg: JsonObject): Single<PublishStatusApiResponse>
}
