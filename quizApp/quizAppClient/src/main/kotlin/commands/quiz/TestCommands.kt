package commands.quiz

enum class TestCommands(val commandStr: String, val argsSize: Int) {
    PROFILE("/profile", 1),
    TEST_INFO("/testinfo", 2),
    GET_TESTS("/tests", 1),
    START_TEST("/start", 2),
    HELP("/help", 1),
    QUIT("/quit", 1),
    LOGOUT("/logout", 1);

    companion object {
        private var map: MutableMap<String, TestCommands> = HashMap()
        init {
            for (v in values()) {
                map[v.commandStr] = v
            }
        }
        fun findByString(str: String): TestCommands? {
            return map[str]
        }
    }
}