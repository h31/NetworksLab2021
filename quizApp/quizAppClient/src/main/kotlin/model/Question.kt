package model

import kotlinx.serialization.Serializable

@Serializable
data class Question(val id: Int, val testId: Int, val value: Int, val questionText: String,
                    val var1: String, val var2: String, val var3: String, val var4: String, val answer: Int) {
    fun getQuestion(): String = buildString {
        appendLine("Value: $value")
        appendLine("Question: $questionText")
        appendLine("    1) $var1")
        appendLine("    2) $var2")
        appendLine("    3) $var3")
        appendLine("    4) $var4")
        append("Answer: ")
    }
}