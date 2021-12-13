package com.monkeys.terminal

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DbConnection {

    fun getConnection(): Connection? {
        try {
            val dbUrl = "jdbc:postgresql://localhost:5432/postgres"
            val user = "postgres"
            val password = "postgres"
            return DriverManager.getConnection(dbUrl, user, password)
        } catch (e: SQLException) {
            println("Error while connecting to DB")
            e.printStackTrace()
        }
        return null
    }

}
