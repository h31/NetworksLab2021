package models

import NOT_IMPL_MSG
import exceptions.NotImplTypeException

sealed class RecordType(val code: Short, private val str: String) {
    class A : RecordType(1, "A")
    class AAAA : RecordType(28, "AAAA")
    class NotImpl(type: Short) : RecordType(type, "$type:NotImpl")
    class MX(val size: Int): RecordType(15, "MX")
    class TXT(val size: Int): RecordType(16, "TXT")

    companion object {
        fun of(code: Short): RecordType = when (code.toInt()) {
            1 -> A()
            28 -> AAAA()
            15 -> MX(0)
            16 -> TXT(0)
            else -> NotImpl(code)
        }
    }

    fun size(): Int = when(this) {
        is A -> 4
        is AAAA -> 16
        is MX -> this.size
        is TXT -> this.size
        is NotImpl -> throw NotImplTypeException(NOT_IMPL_MSG)
    }

    override fun toString(): String {
        return str
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordType

        if (code != other.code) return false
        if (str != other.str) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.toInt()
        result = 31 * result + str.hashCode()
        return result
    }
}

