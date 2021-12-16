package com.monkeys.repo

import com.monkeys.DBConnection
import com.monkeys.getCurrTimestamp
import java.sql.SQLException

class AuthRepo {

    //search for a user in the database
    fun signIn(login: String, password: String): Boolean {
        DBConnection().getConnection().use { connection ->
            val statement = connection!!.createStatement()
            val resSet =
                    statement.executeQuery(
                        "SELECT * FROM \"user\" WHERE (name='$login' AND psw='$password');")
            val res = resSet.next()
            if (res)
                statement.executeUpdate(
                    "UPDATE \"user\" SET active = 'true', last_time_of_activity = '${getCurrTimestamp()}' WHERE name = 'yana';")
            return res
        }
    }

    //try to register new user
    fun signUp(login: String, password: String): Pair<Boolean, String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                statement.execute("INSERT INTO \"user\"(name, psw, active, last_time_of_activity) VALUES ('$login', '$password', 'true', '${getCurrTimestamp()}');")
                Pair(true, "OK")
            } catch (e: SQLException) {
                println("Some problems with registration new user $login")
                Pair(false, e.sqlState)
            }
        }
    }
}