package com.monkeys.controller

import com.monkeys.DBConnection
import com.monkeys.models.AuthModel
import java.sql.CallableStatement
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

class UserController {
    private val clients = Collections.synchronizedList(
        mutableListOf<String>()
    )
    private val activeClients = Collections.synchronizedList(
        mutableListOf<String>()
    )

    fun addUser(model: AuthModel) {
        clients.add(model.login)
        activeClients.add(model.login)
    }

    fun getHierarchy(): List<Pair<String, String>> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = ArrayList<Pair<String, String>>()
                val statement = connection!!.createStatement()
                val set = statement.executeQuery(
                    //getting merged tables of main_themes and sub_themes sorted alphabetically
                    "SELECT main_theme.theme_name, st.theme_name FROM main_theme " +
                            "INNER JOIN sub_theme st on main_theme.id = st.main_theme_id " +
                            "ORDER BY main_theme.theme_name, st.theme_name;")
                while (set.next()) {
                    res.add(Pair(set.getString(1), set.getString(2)))
                }
                return res
            } catch (e: Exception) {
                println("Some")
                ArrayList()
            }
        }
    }

    fun logout(): Boolean {
        return true
    }

    fun getActiveUsers(): List<String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = ArrayList<String>()
                val statement = connection!!.createStatement()
                updateInactiveUsers(statement)
                val set = statement.executeQuery(
                    "SELECT name FROM \"user\" WHERE active = 'true';")
                while (set.next()) {
                    res.add(set.getString(1))
                }
                return res
            } catch (e: Exception) {
                println("Some")
                ArrayList()
            }
        }
    }

    fun getNewMessage() {}

    private fun updateInactiveUsers(statement: Statement) {
        statement.executeUpdate(
            "UPDATE \"user\" SET active = 'false' WHERE last_time_of_activity < now() - '1 hour'::interval;")
    }
}