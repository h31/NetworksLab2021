package model

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val login: String, val lastTestId: Int, val lastResult: Int) {
    override fun toString(): String {
        return "---------PROFILE---------\n" +
                "Username: $login\n" +
                "LastTestId: $lastTestId\n" +
                "Result: $lastResult\n" +
                "-------------------------\n"
    }
}