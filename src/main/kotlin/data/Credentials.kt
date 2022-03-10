package data

import kotlinx.serialization.*


@Serializable
data class Credentials(val login: String, val password: String?)
