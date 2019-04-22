package com.dk.pden.service

import com.dk.pden.service.ApiResponses.ApiResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("v1/search")
    fun groupList(@Query("query") query: String): Single<ApiResponse>

    @GET("v1/users/{user}")
    fun getUser(@Path("user") query: String): Single<JsonObject>
}
