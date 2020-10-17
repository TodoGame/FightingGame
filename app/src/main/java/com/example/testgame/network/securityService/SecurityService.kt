package com.example.testgame.network.securityService

import com.example.testgame.network.UserProperty
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SecurityService {

    @POST("register")
    fun register(@Body registerData: RegisterData):
            Deferred<Response<UserProperty>>

    @POST("login")
    fun login(@Body loginData: LoginData):
            Deferred<Response<UserProperty>>

    @POST("changemypassword")
    fun changePassword(@Body changePasswordData: ChangePasswordData):
            Deferred<Response<UserProperty>>
}