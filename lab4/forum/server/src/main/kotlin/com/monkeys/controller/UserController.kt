package com.monkeys.controller

import com.monkeys.*
import com.monkeys.UserTable.name
import com.monkeys.models.Message
import com.monkeys.models.MessageModel
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.sql.SQLException
import java.sql.Statement
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserController {

    fun getHierarchy(name: String): Pair<Map<String, List<String>>, String> {
        return try {
            val connection = getConnection()!!
            val res = TreeMap<String, List<String>>()
            val table = ArrayList<Pair<String, String>>()
            updateInactiveUsers(connection)
            if (checkActive(connection, name)) {
                connection.from(MainThemeTable)
                    .innerJoin(SubThemeTable, on = MainThemeTable.id eq SubThemeTable.mainThemeId)
                    .select(MainThemeTable.themeName, SubThemeTable.themeName)
                    .orderBy(MainThemeTable.themeName.asc(), SubThemeTable.themeName.asc())
                    .map {
                        table.add(Pair(it[MainThemeTable.themeName].toString(), it[SubThemeTable.themeName].toString()))
                    }
                var main = table[0].first
                var sub = ArrayList<String>()
                table.forEach {
                    if (it.first == main) {
                        sub.add(it.second)
                    } else {
                        res[main] = sub
                        sub = ArrayList()
                        main = it.first
                        sub.add(it.second)
                    }
                }
                res[main] = sub
                Pair(res, "OK")
            } else {
                Pair(HashMap(), "You have been inactive for 1 hour. Login again")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Pair(HashMap(), "Something went wrong, please try again")
        }
    }


    fun getActiveUsers(name: String): Pair<List<String>, String> {
        return try {
            val connection = getConnection()!!
            val res = ArrayList<String>()
            updateInactiveUsers(connection)
            return if (checkActive(connection, name)) {
                connection.from(UserTable).select().where {
                    UserTable.active eq true
                }.map { res.add(it[UserTable.name]!!) }
                Pair(res, "OK")
            } else {
                Pair(ArrayList(), "You have been inactive for 1 hour. Login again")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Pair(ArrayList(), "Something went wrong, please try again")
        }
    }

    fun putNewMessage(name: String, msg: MessageModel): Pair<Boolean, String> {
        return try {
            val connection = getConnection()!!
            updateInactiveUsers(connection)
            return if (checkActive(connection, name)) {
                if (checkIsAThemeExists(connection, msg.subTheme)) {
                    connection.insert(MessageTable) {
                        set(it.text, msg.msg)
                        set(it.userName, name)
                        set(it.time, Instant.now())
                        set(it.subTheme, msg.subTheme)
                    }
                    return Pair(true, "OK")
                }
                Pair(false, "No such sub theme found")
            } else {
                Pair(false, "You have been inactive for 1 hour. Login again")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Pair(false, "Something went wrong, please try again")
        }
    }

    fun getMessages(theme: String, name: String): Pair<List<Message>, String> {
        return try {
            val connection = getConnection()!!
            val res = ArrayList<Message>()
            updateInactiveUsers(connection)
            return if (checkActive(connection, name)) {
                if (checkIsAThemeExists(connection, theme)) {
                    connection.from(MessageTable)
                        .select(MessageTable.time, MessageTable.userName, MessageTable.text)
                        .where {
                            MessageTable.subTheme eq theme
                        }
                        .map {
                            res.add(
                                Message(
                                    it[MessageTable.time]!!.toString(),
                                    it[MessageTable.userName]!!,
                                    it[MessageTable.text]!!
                                )
                            )
                        }
                    return Pair(res, "OK")
                }
                Pair(ArrayList(), "No such sub theme found")
            } else {
                Pair(ArrayList(), "You have been inactive for 1 hour. Login again")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Pair(ArrayList(), "Something went wrong, please try again")
        }
    }

    fun logout(name: String): Pair<Boolean, String> {
        return try {
            val connection = getConnection()!!
            updateInactiveUsers(connection)
            return if (checkActive(connection, name)) {
                connection.update(UserTable) {
                    set(it.active, false)
                    where {
                        UserTable.name eq name
                    }
                }
                Pair(true, "OK")
            } else {
                Pair(true, "You have been inactive for 1 hour. You have already been logged out")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Pair(true, "Something went wrong, please try again")
        }
    }
//        DBConnection().getConnection().use { connection ->
//            return try {
//                val statement = connection!!.createStatement()
//                updateInactiveUsers2(statement)
//                if (checkActive2(statement, name)) {
//                    statement.executeUpdate(
//                        "UPDATE \"user\" SET active = 'false' WHERE name = '${name}'"
//                    )
//                    Pair(true, "OK")
//                } else {
//                    Pair(false, "You have been inactive for 1 hour. You have already been logged out")
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//                Pair(false, "Something went wrong, please try again")
//            }
//        }
//    }

    private fun updateInactiveUsers(connection: Database) {
        connection.update(UserTable) {
            set(it.active, false)
            where {
                UserTable.lastTimeOfActivity less Instant.now().minusSeconds(60 * 60)
            }
        }
    }

    private fun checkActive(connection: Database, name: String): Boolean {
        connection.from(UserTable).select().where {
            UserTable.name eq name
        }.forEach {
            return it[UserTable.active].toString().toBoolean()
        }
        return false
    }

    private fun checkActive2(statement: Statement, name: String): Boolean {
        val set = statement.executeQuery("SELECT active FROM \"user\" WHERE name = '$name';")
        set.next()
        return set.getBoolean(1)
    }

    private fun updateInactiveUsers2(statement: Statement) {
        statement.executeUpdate(
            "UPDATE \"user\" SET active = 'false' WHERE last_time_of_activity < now() - '1 hour'::interval;"
        )
    }

    private fun checkIsAThemeExists(connection: Database, theme: String): Boolean {
        connection.from(SubThemeTable).select().where {
            SubThemeTable.themeName eq theme
        }.forEach {
            return true
        }
        return false
    }

}