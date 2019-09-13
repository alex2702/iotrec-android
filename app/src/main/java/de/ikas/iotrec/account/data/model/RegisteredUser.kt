package de.ikas.iotrec.account.data.model

import com.squareup.moshi.Json

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class RegisteredUser(
    @field:Json(name = "token")
    val token: String,

    @field:Json(name = "username")
    val username: String,

    @field:Json(name = "email")
    val email: String,

    @field:Json(name = "preferences")
    val preferences: MutableList<String>
)
