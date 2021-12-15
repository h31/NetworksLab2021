package model

import kotlinx.serialization.Serializable

@Serializable
data class Question(val id: Int, val testId: Int, val value: Int, val questionText: String,
                    val var1: String, val var2: String, val var3: String, val var4: String, val answer: Int) {
    fun getQuestion(): String {
        return "Value: $value\n" +
                "Question: $questionText\n" +
                "1) $var1\n" +
                "2) $var2\n" +
                "3) $var3\n" +
                "4) $var4"
    }
}