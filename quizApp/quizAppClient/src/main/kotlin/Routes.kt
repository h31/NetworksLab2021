object Routes {
    var PORT: Int = 0
    lateinit var HOST: String
    private const val AUTH = "/auth"
    const val REGISTER = "$AUTH/register"
    const val LOGIN = "$AUTH/login"
    const val TESTS = "/tests"
    const val TEST = "/tests/"
    const val PROFILE = "/users/"
    const val QUESTIONS = "/questions/"
    const val RESULT = "$TESTS/sendAnswers"

    fun getUrl(route: String): String = "http://$HOST:$PORT$route"
}