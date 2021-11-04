package models

import HEADER_SIZE
import byteSubsequence
import bytesToName

data class Question(var qname: String,
                    var qtype: RecordType,
                    var qclass: RecordClass) {
    companion object {
        fun getQuestionFromByteArray(question: ByteArray) : Pair<Question, Int> {
            if (question.size - HEADER_SIZE < 6) throw IllegalArgumentException()

            var i = HEADER_SIZE
            val pair = bytesToName(i, question)
            val qName = pair.first
            i = pair.second
            val qType = byteSubsequence(question, i, i + 2).short
            val qClass = byteSubsequence(question, i + 2, i + 4).short
            return Pair(Question(qName, RecordType.of(qType), RecordClass.of(qClass)), i + 4)
        }
    }
}

