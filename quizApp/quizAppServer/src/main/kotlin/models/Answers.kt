package models
import kotlinx.serialization.Serializable

@Serializable
data class Answers(val answers: List<Int>, val testId: Int, val username: String)
