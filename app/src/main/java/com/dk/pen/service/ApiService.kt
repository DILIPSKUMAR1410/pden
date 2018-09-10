package com.dk.pen.service

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("v1/search")
    fun groupList(@Query("query") query: String): Single<ApiResponse>
}
