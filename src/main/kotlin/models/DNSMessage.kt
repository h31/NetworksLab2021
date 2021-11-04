package models

import models.Header.Companion.getHeaderFromByteArray
import models.Question.Companion.getQuestionFromByteArray
import nameToBytes
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class DNSMessage(var header: Header, var question: Question, var resList: List<Resource>) {

    companion object {
        fun parseByteArray(inData: ByteArray) : DNSMessage {
            val header = getHeaderFromByteArray(inData)
            val qPair = getQuestionFromByteArray(inData)
            val question = qPair.first
            val index = qPair.second
            val resAmount = header.ancount + header.nscount + header.arcount
            val resources = getResourcesFromByteArray(inData, index, resAmount)

            return DNSMessage(header, question, resources)
        }

        private fun getResourcesFromByteArray(inData: ByteArray, index: Int, resAmount: Int): List<Resource> {
            val resList = mutableListOf<Resource>()
            var i = index
            for (j in 0 until resAmount) {
                val rPair = Resource.getResourceFromByteArray(inData, i)
                resList.add(rPair.first)
                i = rPair.second
            }
            return resList
        }
    }

    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeShort(header.id.toInt())

        val flags = header.flags.toUShort()
        dos.writeShort(flags.toInt())

        dos.writeShort(header.qdcount.toInt())
        dos.writeShort(header.ancount.toInt())
        dos.writeShort(header.nscount.toInt())
        dos.writeShort(header.arcount.toInt())
        dos.write(nameToBytes(question.qname))
        dos.writeShort(question.qtype.code.toInt())
        dos.writeShort(question.qclass.code.toInt())
        for (resource in resList) {
            dos.write(nameToBytes(resource.name))
            dos.writeShort(resource.type.code.toInt())
            dos.writeShort(resource.rclass.code.toInt())
            dos.writeInt(resource.ttl)
            dos.writeShort(resource.rdlength.toInt())
            dos.write(Resource().rDataToByteArray(resource.type, resource.rdata, resource.rdlength))
        }
        return baos.toByteArray()
    }

    override fun toString(): String {
        return "DNSMessage(header=$header, question=$question, resList=$resList)"
    }
}