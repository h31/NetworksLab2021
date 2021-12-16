package com.monkeys.controller

import com.monkeys.DBConnection
import com.monkeys.getCurrTimestamp
import com.monkeys.models.AuthModel
import com.monkeys.models.Message
import com.monkeys.models.MessageModel
import com.monkeys.models.ThemeModel
import java.sql.CallableStatement
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserController {

    fun getHierarchy(): Map<String,List<String>> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = TreeMap<String,List<String>>()
                val statement = connection!!.createStatement()
                val set = statement.executeQuery(
                    //getting merged tables of main_themes and sub_themes sorted alphabetically
                    "SELECT main_theme.theme_name, st.theme_name FROM main_theme " +
                            "INNER JOIN sub_theme st on main_theme.id = st.main_theme_id " +
                            "ORDER BY main_theme.theme_name, st.theme_name;")
                set.next()
                var main = set.getString(1)
                var sub = ArrayList<String>()
                sub.add(set.getString(2))
                while (set.next()) {
                    if (set.getString(1) == main) {
                        sub.add(set.getString(2))
                    } else {
                        res[main] = sub
                        sub = ArrayList<String>()
                        main = set.getString(1)
                        sub.add(set.getString(2))
                    }
                }
                res[main] = sub
                return res
            } catch (e: Exception) {
                println("Some")
                HashMap()
            }
        }
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

    fun putNewMessage(user: AuthModel, msg: MessageModel): Boolean {
        DBConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                val set = statement.executeQuery(
                    "SELECT theme_name FROM sub_theme WHERE theme_name = '${msg.subTheme}';")
                while (set.next()) {
                    statement.execute("INSERT INTO message (text, user_name, time, sub_theme) VALUES ('${msg.msg}', '${user.login}', '${getCurrTimestamp()}', '${msg.subTheme}');")
                    return true
                }
                return false
            } catch (e: Exception) {
                println("Some")
                false
            }
        }
    }

    fun getMessages(msg: ThemeModel): List<Message> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = ArrayList<Message>()
                val statement = connection!!.createStatement()
                var set = statement.executeQuery(
                    "SELECT theme_name FROM sub_theme WHERE theme_name = '${msg.subTheme}';")
                while (set.next()) {
                    set = statement.executeQuery(
                        "SELECT time, user_name, text FROM message WHERE sub_theme = '${msg.subTheme}';")
                    while (set.next()) {
                        res.add(Message(set.getString(1),
                            set.getString(2),
                            set.getString(3)))
                    }
                    return res
                }
                return ArrayList()
            } catch (e: Exception) {
                println("Some")
                ArrayList()
            }
        }
    }

    fun logout(name: String): Boolean {
        DBConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                statement.executeUpdate(
                    "UPDATE \"user\" SET active = 'false' WHERE name = '${name}'")
                true
            } catch (e: Exception) {
                println("Some")
                false
            }
        }
    }

    private fun updateInactiveUsers(statement: Statement) {
        statement.executeUpdate(
            "UPDATE \"user\" SET active = 'false' WHERE last_time_of_activity < now() - '1 hour'::interval;")
        TODO("добавить принудительное удаление юзера")

    }
}