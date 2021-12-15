package model

import io.ktor.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class User(val login: String, val password: String) : Principal