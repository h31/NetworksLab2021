package commands.auth


enum class AuthCommands(private val commandStr: String, val argsSize: Int) {
    REGISTER("/register", 4),
    LOGIN("/login", 3),
    HELP("/help", 1),
    QUIT("/quit", 1);

    companion object {
        private var map: MutableMap<String, AuthCommands> = HashMap()
        init {
            for (v in values()) {
                map[v.commandStr] = v
            }
        }
        fun findByString(str: String): AuthCommands? {
            return map[str]
        }
    }
}