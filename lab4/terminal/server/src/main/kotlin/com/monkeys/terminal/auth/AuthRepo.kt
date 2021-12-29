package com.monkeys.terminal.auth

import com.monkeys.terminal.DbConnection
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.Roles

class AuthRepo {

    fun signIn(login: String, password: String): AuthModel? {
        DbConnection().getConnection().use { connection ->
            val ps = connection!!.prepareStatement("SELECT * FROM users WHERE (LOGIN=? AND PASSWORD=?);").apply {
                setString(1, login)
                setString(2, password)
            }
            val resSet = ps.executeQuery()
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
                val ps = connection!!.prepareStatement("INSERT INTO users(LOGIN, PASSWORD, ROLE) VALUES (?, ?, ?);").apply {
                    setString(1, login)
                    setString(2, password)
                    setString(3, role)
                }
                ps.execute()
                true
            } catch (e: Exception) {
                println("Some problems with registration new user $login")
                false
            }
        }
    }

}
