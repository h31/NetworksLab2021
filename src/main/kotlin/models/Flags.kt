package models

import SPACE_CHARACTER
import ZERO
import getCharBitFromBool
import getBitsFromShort
import getBoolFromCharBit

data class Flags(var qr: Boolean = false, var opcode: OperationCode = OperationCode.of(0),
                 var aa: Boolean = false, var tc: Boolean = false, var rd: Boolean = false,
                 var ra: Boolean = false, var z: Short = 0, var rcode: ResponseCode = ResponseCode.of(0)) {

    companion object {
        fun ushortToFlags(flags: UShort): Flags {
            val str = String.format("%" + 16 + "s", flags.toString(radix = 2)).replace(SPACE_CHARACTER, ZERO)
            val qr = getBoolFromCharBit(str[0])
            val opCode = str.substring(1, 5).toShort(radix = 2)
            val aa = getBoolFromCharBit(str[5])
            val tc = getBoolFromCharBit(str[6])
            val rd = getBoolFromCharBit(str[7])
            val ra = getBoolFromCharBit(str[8])
            val z = str.substring(9, 12).toShort(radix = 2)
            val rCode = str.substring(12, 16).toShort(radix = 2)
            return Flags(qr, OperationCode.of(opCode), aa, tc, rd, ra, z, ResponseCode.of(rCode))
        }
    }

    fun toUShort(): UShort {
        val flagsStr = getCharBitFromBool(this.qr) +
                getBitsFromShort(this.opcode.code) +
                getCharBitFromBool(this.aa) +
                getCharBitFromBool(this.tc) +
                getCharBitFromBool(this.rd) +
                getCharBitFromBool(this.ra) +
                ZERO.repeat(3) + //z here - always 000
                getBitsFromShort(this.rcode.code)
        return flagsStr.toUShort(radix = 2)
    }
}