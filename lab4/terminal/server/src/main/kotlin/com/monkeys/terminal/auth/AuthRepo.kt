package com.monkeys.terminal.auth

import com.monkeys.terminal.DbConnection
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.Roles

class AuthRepo {

    fun signIn(login: String, password: String): AuthModel? {
        DbConnection().getConnection().use { connection ->
            val statement = connection!!.createStatement()
            val resSet =
                statement.executeQuery("SELECT * FROM users WHERE (LOGIN='$login' AND PASSWORD='$password');")
            return if (resSet.next()) {
                val role = resSet.getString("role")
                AuthModel(login, "", role)
            } else
                null

        }
    }

    fun signUp(login: String, password: String, role: String): Boolean {
        DbConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                statement.execute("INSERT INTO users(LOGIN, PASSWORD, ROLE) VALUES ('$login', '$password', '$role');")
                true
            } catch (e: Exception) {
                println("Some problems with registration new user $login")
                false
            }
        }
    }

}
