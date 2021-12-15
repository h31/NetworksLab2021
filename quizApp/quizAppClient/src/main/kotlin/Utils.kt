import org.apache.commons.validator.routines.InetAddressValidator
import java.util.*

const val DEFAULT_PORT = 8080
const val DEFAULT_HOST = "26.11.70.132"


fun isValidIP(ip: String): Boolean {
    val validator = InetAddressValidator.getInstance()
    if (validator.isValid(ip) || ip.lowercase(Locale.getDefault()) == "localhost")
        return true
    return false
}