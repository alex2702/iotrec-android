package de.ikas.iotrec.account.data.model

import com.squareup.moshi.Json
import de.ikas.iotrec.database.model.Experiment
import de.ikas.iotrec.database.model.Preference

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class RegisteredUser(
    @field:Json(name = "id")
    val id: Int,

    @field:Json(name = "token")
    val token: String,

    @field:Json(name = "username")
    val username: String,

    @field:Json(name = "email")
    val email: String,

    @field:Json(name = "preferences")
    val preferences: MutableList<Preference>,

    @field:Json(name = "experiments")
    val experiments: MutableList<Experiment>
)
