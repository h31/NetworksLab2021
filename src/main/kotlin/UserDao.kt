object UserDao {
    private val users = listOf(
        User(name = "Ilia", password = "qwerty123", admin = true),
        User(name = "player1", password = "123", admin = false),
        User(name = "player2", password = "123", admin = false),
        User(name = "player3", password = "123", admin = false)
    )

    fun findByName(name: String): User? {
        return users.find { it.name == name }
    }

    fun checkUser(user: User?): Boolean = user != null && users.contains(user)
}