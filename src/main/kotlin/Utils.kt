import java.nio.ByteBuffer
import kotlin.random.Random

const val NOT_IMPL_MSG = "This record type is not supported."
const val RECORD_FILE_PATH = "src/main/NameLists/"
const val MAX_PACKET_SIZE = 512
const val SPACE_CHARACTER = " "
const val COLON_CHARACTER = ":"
const val DOT_CHARACTER = "."
const val HEADER_SIZE = 12
const val PORT = 53
const val ZERO = "0"

fun rndShort(): Short = Random.nextInt(Short.MAX_VALUE + 1).toShort()

fun getBoolFromBit(char: Char): Boolean = char == '1'

fun getBitsFromShort(inShort: Short): String {
    val strBytes = shortToString(inShort)
    return shortToString(inShort).substring(strBytes.length - 4, strBytes.length)
}

fun shortToString(inShort: Short): String =
    String.format("%" + 16 + "s", inShort.toString(radix = 2)).replace(SPACE_CHARACTER.toRegex(), ZERO)

fun byteToString(inByte: Byte): String =
    String.format("%" + 8 + "s", inByte.toUByte().toString(radix = 2)).replace(SPACE_CHARACTER.toRegex(), ZERO)

fun getBit(value: Int, position: Int): Int = (value shr position) and 1

fun byteToInt(inByte: Byte): Int = inByte.toInt() and 0xff

fun byteToHex(inByte: Byte): String {
    val intByteStr = (inByte.toInt() and 0xff).toString(radix = 16)
    return String.format("%" + 2 + "s", intByteStr).replace(SPACE_CHARACTER.toRegex(), ZERO)
}


fun getBitFromBool(inBool: Boolean): Char = if (inBool) '1' else '0'

fun byteSubsequence(array: ByteArray, start: Int, end : Int): ByteBuffer =
    ByteBuffer.wrap(array.copyOfRange(start, end))

fun nameToBytes(inName: String): ByteArray {
    //my.domain.at.com -> my domain at com -> 2 M Y 6 D O M A I N 2 A T 3 C O M 0
    val parsedDomain = inName.split(DOT_CHARACTER)
    val qName = ByteArray(inName.length + 2)
    var byteIter = 0
    for (subDomain in parsedDomain) {
        qName[byteIter] = subDomain.length.toByte()
        byteIter++
        for (letter in subDomain) {
            qName[byteIter] = letter.toByte()
            byteIter++
        }
    }
    qName[byteIter] = 0
    return qName
}

//2 M Y 6 D O M A I N 2 A T 3 C O M 0 -> my.domain.at.com
//2 M Y 6 D O M A I N 2 A T 3 C O M 0 ...... 2 M X 11000000 00001100 (pointer to 12) -> mx.my.domain.at.com
fun bytesToName(initPointer: Int, inData: ByteArray): Pair<String, Int> {
    var name = String()
    var i = initPointer
    var end = 0
    var readAmount = -1
    var pointerFound = false
    while (readAmount != 0) {
        val currByte = inData[i]
        if (!pointerFound) {
            val isPointer = getBit(currByte.toInt(), 7) == 1 && getBit(currByte.toInt(), 6) == 1
            if (isPointer) {
                end = i + 2
                i = ("00" + byteToString(currByte).substring(2, 7) + inData[i + 1].toString(radix = 2)).toInt(radix = 2)
                pointerFound = true
                continue
            }
        }
        readAmount = currByte.toInt()
        i++
        if (readAmount == 0) break
        for (j in 0 until readAmount) {
            name += inData[i].toChar()
            i++
        }
        if (inData[i].toInt() != 0) name += DOT_CHARACTER
    }
    if (!pointerFound) end = i
    return Pair(name, end)
}

fun shortToByteArray(inShort: Short): ByteArray {
    val buffer = ByteBuffer.allocate(2)
    buffer.putShort(inShort)
    return buffer.array()
}