package model

import kotlinx.serialization.Serializable

@Serializable
data class Question(val id: Int, val testId: Int, val value: Int, val questionText: String,
                    val answersList: List<String>, val answer: Int) {
    fun getQuestion(): String = buildString {
        appendLine("Value: $value")
        appendLine("Question: $questionText")
        for ((i, s) in answersList.withIndex()) {
            appendLine("    ${i+1}) $s")
        }
        append("Answer: ")
    }
}