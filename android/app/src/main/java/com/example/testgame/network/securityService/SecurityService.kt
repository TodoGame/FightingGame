package com.example.testgame.network.securityService

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface SecurityService {

    @POST("register")
    fun register(@Body registerData: RegisterData):
        Deferred<Response<UserProperty>>

    @POST("login")
    fun login(@Body loginData: LoginData):
        Deferred<Response<UserProperty>>

    @POST("changemypassword")
    fun changePassword(@Body changePasswordData: ChangePasswordData, @HeaderMap headers: Map<String, String>):
        Deferred<Response<Void>>
}
