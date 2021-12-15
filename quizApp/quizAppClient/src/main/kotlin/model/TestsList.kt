package model

import kotlinx.serialization.Serializable

@Serializable
data class TestsList(val testsList: List<Test>) {
    override fun toString(): String {
        var str = "---------TESTS---------\n"
        for (test in testsList) {
            str += "$test\n"
        }
        if (testsList.isEmpty()) {
            str += "No test for this time"
        }
        str += "-----------------------"
        return str
    }
}