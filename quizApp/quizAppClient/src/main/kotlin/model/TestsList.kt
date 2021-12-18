package model

import kotlinx.serialization.Serializable

@Serializable
data class TestsList(val testsList: List<Test>) {
    override fun toString(): String = buildString {
        appendLine("-----------TESTS-----------")
        for (i in testsList.indices) {
            if (i != testsList.size - 1) {
                appendLine("${testsList[i]}\n")
            }
            else appendLine(testsList[i].toString())
        }
        if (testsList.isEmpty())
            appendLine("No test for this time")
        appendLine("-------------------------")
    }
}