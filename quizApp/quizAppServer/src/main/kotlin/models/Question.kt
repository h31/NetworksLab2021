package models
import kotlinx.serialization.Serializable

@Serializable
data class Question(val id: Int, val testId: Int, val value: Int, val questionText: String,
                    val answersList: List<String>, val answer: Int)
