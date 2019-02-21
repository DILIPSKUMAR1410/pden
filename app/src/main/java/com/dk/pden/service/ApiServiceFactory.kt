package com.dk.pden.service

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object ApiServiceFactory {

    fun createService(): ApiService = Retrofit.Builder()
            .baseUrl("https://core.blockstack.org/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ApiService::class.java)

    fun createFirebaseService(): FirebaseService = Retrofit.Builder()
            .baseUrl("https://246e7fe4-7a7b-4da5-9c76-d1cc8d1c4bac.pushnotifications.pusher.com/publish_api/v1/instances/246e7fe4-7a7b-4da5-9c76-d1cc8d1c4bac/publishes/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(FirebaseService::class.java)

}