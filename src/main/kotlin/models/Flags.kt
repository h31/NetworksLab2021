package models

import SPACE_CHARACTER
import ZERO
import getBitFromBool
import getBitsFromShort
import getBoolFromBit

data class Flags(var qr: Boolean = false, var opcode: OperationCode = OperationCode.of(0),
                 var aa: Boolean = false, var tc: Boolean = false, var rd: Boolean = false,
                 var ra: Boolean = false, var z: Short = 0, var rcode: ResponseCode = ResponseCode.of(0)) {

    companion object {
        fun ushortToFlags(flags: UShort): Flags {
            val str = String.format("%" + 16 + "s", flags.toString(radix = 2)).replace(SPACE_CHARACTER, ZERO)
            val qr = getBoolFromBit(str[0])
            val opCode = str.substring(1, 5).toShort(radix = 2)
            val aa = getBoolFromBit(str[5])
            val tc = getBoolFromBit(str[6])
            val rd = getBoolFromBit(str[7])
            val ra = getBoolFromBit(str[8])
            val z = str.substring(9, 12).toShort(radix = 2)
            val rCode = str.substring(12, 16).toShort(radix = 2)
            return Flags(qr, OperationCode.of(opCode), aa, tc, rd, ra, z, ResponseCode.of(rCode))
        }
    }

    fun toUShort(): UShort {
        val flagsStr = getBitFromBool(this.qr) +
                getBitsFromShort(this.opcode.code) +
                getBitFromBool(this.aa) +
                getBitFromBool(this.tc) +
                getBitFromBool(this.rd) +
                getBitFromBool(this.ra) +
                ZERO.repeat(3) + //z here - always 000
                getBitsFromShort(this.rcode.code)
        return flagsStr.toUShort(radix = 2)
    }
}