package com.poly.dnshelper.model

import com.poly.dnshelper.Util.getBytesFromShort
import com.poly.dnshelper.Util.getShortFromTwoBytes
import com.poly.dnshelper.model.answer.*

data class DNSMessage(
    var transactionId: Short = 0, // 16 bits
    var dnsFlags: DNSFlags = DNSFlags(), // 16 bits
    var numOfQuestions: Short = 0, // 16 bits
    var answerRRs: Short = 0, // 16 bits
    var authorityRRs: Short = 0, // 16 bits
    var additionalRRs: Short = 0, // 16 bits
    var questions: List<DNSQuery> = listOf(),
    var answers: List<DNSAnswer> = listOf(),
) {

    fun getMessageBytes(): ByteArray {
        val resultArrayBytes = mutableListOf<Byte>()
        resultArrayBytes.addAll(getBytesFromShort(transactionId))
        resultArrayBytes.addAll(getBytesFromShort(dnsFlags.getBytes()))
        resultArrayBytes.addAll(getBytesFromShort(numOfQuestions))
        resultArrayBytes.addAll(getBytesFromShort(answerRRs))
        resultArrayBytes.addAll(getBytesFromShort(authorityRRs))
        resultArrayBytes.addAll(getBytesFromShort(additionalRRs))
        for (question in questions) {
            resultArrayBytes.addAll(question.getQueryBytes())
        }
        for (answer in answers) {
            resultArrayBytes.addAll(answer.getAnswerBytes())
        }
        return resultArrayBytes.toByteArray()
    }

    fun mapperMessage(byteArray: ByteArray, prevMessage: DNSMessage? = null) {
        transactionId = getShortFromTwoBytes(byteArray.slice(0..1).toByteArray())
        dnsFlags.mapperFlags(byteArray.slice(2..3).toByteArray())
        numOfQuestions = getShortFromTwoBytes(byteArray.slice(4..5).toByteArray())
        answerRRs = getShortFromTwoBytes(byteArray.slice(6..7).toByteArray())
        authorityRRs = getShortFromTwoBytes(byteArray.slice(8..9).toByteArray())
        additionalRRs = getShortFromTwoBytes(byteArray.slice(10..11).toByteArray())
        val sizeQuestions = prevMessage?.getMessageBytes()?.size ?: byteArray.size
        val dnsQuery = DNSQuery()
        dnsQuery.mapperQuery(byteArray.slice(12..sizeQuestions).toByteArray())
        questions = listOf(dnsQuery)
        answers = getAnswers(answerRRs, byteArray, prevMessage)
    }

    private fun getAnswers(answerRRs: Short, byteArray: ByteArray, prevMessage: DNSMessage?): List<DNSAnswer> {
        val answers = mutableListOf<DNSAnswer>()
        if (prevMessage != null) {
            var answerSize = 0
            var currentPosition = prevMessage.getMessageBytes().size
            for (i in 0 until answerRRs) {
                currentPosition += answerSize
                val dnsAnswer = when (prevMessage.questions[0].type) {
                    (1).toShort() -> DNSAnswerA()
                    (15).toShort() -> DNSAnswerMX()
                    (16).toShort() -> DNSAnswerTXT()
                    (28).toShort() -> DNSAnswerAAAA()
                    else -> {
                        throw IllegalArgumentException()
                    }
                }
                answerSize =
                    dnsAnswer.getSize(byteArray.slice(currentPosition..byteArray.size).toByteArray())
                dnsAnswer.mapperAnswer(
                    byteArray
                        .toList()
                        .subList(currentPosition, currentPosition + answerSize)
                        .toByteArray()
                )
                answers.add(dnsAnswer)
            }
        }
        return answers
    }
}
