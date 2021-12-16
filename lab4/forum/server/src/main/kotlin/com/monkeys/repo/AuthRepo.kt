package com.monkeys.repo

import com.monkeys.DBConnection

class AuthRepo {

    //search for a user in the database
    fun signIn(login: String, password: String): Boolean {
        DBConnection().getConnection().use { connection ->
            val statement = connection!!.createStatement()
            val resSet =
                    statement.executeQuery("SELECT * FROM \"user\" WHERE (name='$login' AND psw='$password');")
            return resSet.next()
        }
    }

    //try to register new user
    fun signUp(login: String, password: String): Boolean {
        DBConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                statement.execute("INSERT INTO \"user\"(name, psw) VALUES ('$login', '$password');")
                true
            } catch (e: Exception) {
                println("Some problems with registration new user $login")
                false
            }
        }
    }
}