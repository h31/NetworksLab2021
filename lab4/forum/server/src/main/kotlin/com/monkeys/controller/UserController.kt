package com.monkeys.controller

import com.monkeys.*
import com.monkeys.models.Message
import com.monkeys.models.MessageModel
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.lang.IllegalArgumentException
import java.sql.SQLException
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class UserController {

    fun getHierarchy(name: String): Map<String, List<String>> {
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
                res
            } else {
                throw Exception("You have been inactive for 1 hour. Login again")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Something went wrong, please try again")
        }
    }

    fun getActiveUsers(name: String): List<String> {
        try {
            val connection = getConnection()!!
            val res = ArrayList<String>()
            updateInactiveUsers(connection)
            if (checkActive(connection, name)) {
                connection.from(UserTable).select().where {
                    UserTable.active eq true
                }.map { res.add(it[UserTable.name]!!) }
                return res
            }
            throw Exception("You have been inactive for 1 hour. Login again")
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Something went wrong, please try again")
        }
    }

    fun putNewMessage(name: String, msg: MessageModel): Boolean {
        try {
            val connection = getConnection()!!
            updateInactiveUsers(connection)
            if (checkActive(connection, name)) {
                if (checkIsAThemeExists(connection, msg.subTheme)) {
                    connection.insert(MessageTable) {
                        set(it.text, msg.msg)
                        set(it.userName, name)
                        set(it.time, Instant.now())
                        set(it.subTheme, msg.subTheme)
                    }
                    return true
                }
                throw IllegalArgumentException("No such sub theme found")
            }
            throw Exception("You have been inactive for 1 hour. Login again")
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Something went wrong, please try again")
        }
    }

    fun getMessages(theme: String, name: String): List<Message> {
        try {
            val connection = getConnection()!!
            val res = ArrayList<Message>()
            updateInactiveUsers(connection)
            if (checkActive(connection, name)) {
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
                    return res
                }
                throw IllegalArgumentException("No such sub theme found")
            }
            throw Exception("You have been inactive for 1 hour. Login again")
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Something went wrong, please try again")
        }
    }

    fun logout(name: String): Boolean {
        try {
            val connection = getConnection()!!
            updateInactiveUsers(connection)
            if (checkActive(connection, name)) {
                connection.update(UserTable) {
                    set(it.active, false)
                    where {
                        UserTable.name eq name
                    }
                }
                return true
            }
            throw Exception("You have been inactive for 1 hour. You have already been logged out")
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Something went wrong, please try again")
        }
    }

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

    private fun checkIsAThemeExists(connection: Database, theme: String): Boolean {
        connection.from(SubThemeTable).select().where {
            SubThemeTable.themeName eq theme
        }.forEach {
            return true
        }
        return false
    }

}