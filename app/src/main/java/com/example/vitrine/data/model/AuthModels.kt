
package com.example.vitrine.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val data: RegisterMetadata
)

data class RegisterMetadata(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("group_code") val groupCode: String,
    val phone: String? = null
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?,
    @SerializedName("token_type") val tokenType: String?,
    val user: AuthUser?
)

data class AuthUser(
    val id: String,
    val email: String?,
    @SerializedName("user_metadata") val userMetadata: UserMetadata?
)

data class UserMetadata(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("group_code") val groupCode: String?
)
