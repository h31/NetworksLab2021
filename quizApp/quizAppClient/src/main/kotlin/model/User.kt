package model

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val login: String, val lastTestId: Int, val lastResult: Int) {
    override fun toString(): String = buildString {
        appendLine("---------PROFILE---------")
        appendLine("Username: $login")
        appendLine("LastTestId: $lastTestId")
        appendLine("Result: $lastResult")
        appendLine("-------------------------")
    }
}