package model

import kotlinx.serialization.Serializable

@Serializable
data class Test(val id: Int, val name: String, val desc: String) {
    override fun toString(): String = buildString {
        appendLine("ID: $id    Name: $name")
        appendLine("Description: $desc")
    }
}