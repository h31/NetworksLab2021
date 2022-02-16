import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException


val algorithm: Algorithm = Algorithm.HMAC256("OnlineRoulette")

object Authorization {
    private val verifier = JWT.require(algorithm)
        .withIssuer("OnlineRoulette")
        .build()

    fun generateToken(user: User): String = JWT.create()
        .withIssuer("OnlineRoulette")
        .withClaim("name", user.name)
        .withClaim("password", user.password)
        .sign(algorithm)

    fun checkToken(token: String): User? =
        try {
            verifier.verify(token).claims["name"]?.asString()?.let { UserDao.findByName(it) }
        } catch (e: SignatureVerificationException) {
            null
        }
}