package models

import byteSubsequence

data class Header(var id: Short = 0,
                  var flags: Flags = Flags(),
                  var qdcount: Short = 1,
                  var ancount: Short = 0,
                  var nscount: Short = 0,
                  var arcount: Short = 0) {

    companion object {
        fun getHeaderFromByteArray(headerBytes: ByteArray) : Header {
            if (headerBytes.size < 12) throw IllegalArgumentException()

            val id = byteSubsequence(headerBytes, 0, 2).short
            val flags = Flags.ushortToFlags(byteSubsequence(headerBytes, 2, 4).short.toUShort())
            val qdCount = byteSubsequence(headerBytes, 4, 6).short
            val anCount = byteSubsequence(headerBytes, 6, 8).short
            val nsCount = byteSubsequence(headerBytes, 8, 10).short
            val arCount = byteSubsequence(headerBytes, 10, 12).short
            return Header(id, flags, qdCount, anCount, nsCount, arCount)
        }
    }
}