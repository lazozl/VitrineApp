package com.example.vitrine.data.api

import com.example.vitrine.data.model.AuthResponse
import com.example.vitrine.data.model.LoginRequest
import com.example.vitrine.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/v1/signup")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
