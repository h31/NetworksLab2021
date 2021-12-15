class Routes {
    companion object {
        private const val BASE_URL = "http://$DEFAULT_HOST:$DEFAULT_PORT"
        const val REGISTER = "$BASE_URL/auth/register"
        const val LOGIN = "$BASE_URL/auth/login"
        const val GET_TESTS = "$BASE_URL/tests"
        const val GET_TEST = "$BASE_URL/tests/"
        const val PROFILE = "$BASE_URL/users/"
        const val GET_QUESTIONS = "$BASE_URL/questions"
        const val GET_RESULT = "$GET_TESTS/sendAnswers"
    }
}