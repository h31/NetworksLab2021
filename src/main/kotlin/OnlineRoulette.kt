import kotlin.random.Random


class OnlineRoulette {
    private val tableBets: MutableList<Pair<User, Bet>> = mutableListOf()
    private val tableResults: MutableList<Pair<User, Int>> = mutableListOf()
    private var results: Pair<Int, String> = Pair(-1, "nothing")

    fun addBet(user: User, bet: Bet): Boolean {
        return if (bet.bet > 0 && (bet.type == "odd" || bet.type == "even" || (bet.type == "number" && bet.number in 0..36))) {
            tableBets.add(user to bet)
            true
        } else false
    }

    fun getBets(): List<Bet> {
        return tableBets.map { it.second }
    }

    fun gamble() {
        val number = Random.nextInt(37)
        results = Pair(number, if (number % 2 == 0) "even" else "odd")
        tableBets.forEach {
            if (it.second.type == results.second) {
                tableResults.add(Pair(it.first, it.second.bet * 2))
            } else if (it.second.type == "number" && it.second.number == results.first) {
                tableResults.add(Pair(it.first, it.second.bet * 4))
            } else {
                tableResults.add(Pair(it.first, -it.second.bet))
            }
        }
        tableBets.clear()
    }

    fun getResult(): Int = results.first

    fun getUserResult(user: User): Int = tableResults.filter { it.first == user }.sumOf { it.second }
}