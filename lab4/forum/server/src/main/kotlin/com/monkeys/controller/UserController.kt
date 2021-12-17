package com.monkeys.controller

import com.monkeys.DBConnection
import com.monkeys.getCurrTimestamp
import com.monkeys.models.AuthModel
import com.monkeys.models.Message
import com.monkeys.models.MessageModel
import com.monkeys.models.ThemeModel
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserController {

    fun getHierarchy(name: String): Pair<Map<String,List<String>>, String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = TreeMap<String,List<String>>()
                val statement = connection!!.createStatement()
                updateInactiveUsers(statement)
                if (checkActive(statement, name)) {
                    val set = statement.executeQuery(
                        //getting merged tables of main_themes and sub_themes sorted alphabetically
                        "SELECT main_theme.theme_name, st.theme_name FROM main_theme " +
                                "INNER JOIN sub_theme st on main_theme.id = st.main_theme_id " +
                                "ORDER BY main_theme.theme_name, st.theme_name;"
                    )
                    set.next()
                    var main = set.getString(1)
                    var sub = ArrayList<String>()
                    sub.add(set.getString(2))
                    while (set.next()) {
                        if (set.getString(1) == main) {
                            sub.add(set.getString(2))
                        } else {
                            res[main] = sub
                            sub = ArrayList()
                            main = set.getString(1)
                            sub.add(set.getString(2))
                        }
                    }
                    res[main] = sub
                    return Pair(res, "OK")
                } else {
                    Pair(HashMap(), "You have been inactive for 1 hour. Login again")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Pair(HashMap(), "Something went wrong, please try again")
            }

        }
    }

    fun getActiveUsers(name: String): Pair<List<String>, String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = ArrayList<String>()
                val statement = connection!!.createStatement()
                updateInactiveUsers(statement)
                if (checkActive(statement, name)) {
                    val set = statement.executeQuery(
                        "SELECT name FROM \"user\" WHERE active = 'true';"
                    )
                    while (set.next()) {
                        res.add(set.getString(1))
                    }
                    return Pair(res, "OK")
                } else {
                    Pair(ArrayList(), "You have been inactive for 1 hour. Login again")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Pair(ArrayList(), "Something went wrong, please try again")
            }
        }
    }

    fun putNewMessage(name: String, msg: MessageModel): Pair<Boolean, String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                updateInactiveUsers(statement)
                if (checkActive(statement, name)) {
                    if (checkIsAThemeExists(statement, msg.subTheme)) {
                        statement.execute("INSERT INTO message (text, user_name, time, sub_theme) VALUES ('${msg.msg}', '${name}', '${getCurrTimestamp()}', '${msg.subTheme}');")
                        return Pair(true, "OK")
                    }
                    return Pair(false, "No such sub theme found")
                } else {
                    Pair(false, "You have been inactive for 1 hour. Login again")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Pair(false, "Something went wrong, please try again")
            }
        }
    }

    fun getMessages(msg: ThemeModel, name: String): Pair<List<Message>,String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val res = ArrayList<Message>()
                val statement = connection!!.createStatement()
                updateInactiveUsers(statement)
                if (checkActive(statement, name)) {
                    if (checkIsAThemeExists(statement, msg.subTheme)) {
                        val set = statement.executeQuery(
                            "SELECT time, user_name, text FROM message WHERE sub_theme = '${msg.subTheme}';"
                        )
                        while (set.next()) {
                            res.add(
                                Message(
                                    set.getString(1),
                                    set.getString(2),
                                    set.getString(3)
                                )
                            )
                        }
                        return Pair(res, "OK")
                    }
                    return Pair(ArrayList(), "No such sub theme found")
                } else {
                    return Pair(ArrayList(), "You have been inactive for 1 hour. Login again")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Pair(ArrayList(), "Something went wrong, please try again")
            }
        }
    }

    fun logout(name: String): Pair<Boolean,String> {
        DBConnection().getConnection().use { connection ->
            return try {
                val statement = connection!!.createStatement()
                updateInactiveUsers(statement)
                if (checkActive(statement, name)) {
                    statement.executeUpdate(
                        "UPDATE \"user\" SET active = 'false' WHERE name = '${name}'"
                    )
                    Pair(true, "OK")
                } else {
                    Pair(false, "You have been inactive for 1 hour. You have already been logged out")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Pair(false, "Something went wrong, please try again")
            }
        }
    }

    private fun updateInactiveUsers(statement: Statement) {
        statement.executeUpdate(
            "UPDATE \"user\" SET active = 'false' WHERE last_time_of_activity < now() - '1 hour'::interval;")
    }

    private fun checkIsAThemeExists(statement: Statement, theme: String): Boolean {
        val set = statement.executeQuery(
            "SELECT theme_name FROM sub_theme WHERE theme_name = '${theme}';")
        return set.next()
    }

    private fun checkActive(statement: Statement, name: String): Boolean {
        val set = statement.executeQuery("SELECT active FROM \"user\" WHERE name = '$name';")
        set.next()
        return set.getBoolean(1)
    }
}