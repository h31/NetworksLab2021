import org.junit.Test

class DNSQueryTest {

    @Test
    fun nameFromByteTest() {
        val byteArray = byteArrayOf(0x03, 0x44, 0x44, 0x44, 0x02, 0x45, 0x45, 0x00)
//        println(DNSQuery().nameFromBytes(byteArray))
    }
}