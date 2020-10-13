package com.example.android.marsrealestate.network

import com.squareup.moshi.Json

data class UserProperty(
        val id: String,
        @Json(name = "img_src") val imgSrcUrl: String,
        val username: String,
        val password: Double)
