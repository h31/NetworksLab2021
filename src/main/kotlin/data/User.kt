package data

import kotlinx.serialization.*


@Serializable
data class User(val login: String, val access_level: String)
