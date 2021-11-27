import com.poly.dnshelper.model.DNSFlags
import org.junit.Assert
import org.junit.Test

class DNSFlagsTest {

    @Test
    fun getFinalResultTest() {
        val dnsFlags = DNSFlags(
            isResponse = true,
            opCode = 1,
            aa = true,
            truncated = true,
            recursionDesired = false,
            recursionAccepted = true,
            rCode = 8
        )
        val bytes = dnsFlags.getBytes()
        Assert.assertEquals("1000111010001000", Integer.toBinaryString(bytes.toInt()).substring(16))
    }
}