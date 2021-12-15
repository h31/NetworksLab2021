package model

import kotlinx.serialization.Serializable

@Serializable
data class Test(val id: Int, val name: String, val desc: String) {
    override fun toString(): String {
        return "ID:$id    Name: $name\nDescription: $desc"
    }
}