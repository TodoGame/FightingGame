package com.example.testgame.network

import com.example.testgame.network.securityService.SecurityService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

private const val BASE_URL = "http://0.0.0.0:8080/"

private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()

object SecurityApi {
    val RETROFIT_SERVICE : SecurityService by lazy {
        retrofit.create(SecurityService::class.java)
    }
}
