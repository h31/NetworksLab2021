package com.monkeys

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DBConnection {

    private val user = "postgres"
    private val password = "123456"
    private val url = "jdbc:postgresql://localhost:5432/forum"

    fun getConnection(): Connection? {
        try {
            return DriverManager.getConnection(url, user, password)
        } catch (e: SQLException) {
            println("Error while connecting to DB")
            e.printStackTrace()
        }
        return null
    }
}