package com.monkeys.repo

import com.monkeys.UserTable
import com.monkeys.getConnection
import com.monkeys.password
import org.ktorm.dsl.*
import java.sql.SQLException
import java.time.Instant

class AuthRepo {

    //search for a user in the database
    fun signIn(login: String, password: String): Boolean {
        return try {
            val connection = getConnection()!!
            val exists = connection.from(UserTable).select().where {
                (UserTable.name eq login) and (UserTable.psw eq password)
            }
            if (exists.totalRecords == 0)
                return false
            connection.update(UserTable) {
                set(it.active, true)
                set(it.lastTimeOfActivity, Instant.now())
                where {
                    (UserTable.name eq login) and (UserTable.psw eq password)
                }
            }
            true
        } catch (e: SQLException) {
            e.printStackTrace()
            println(e.message)
            false
        }
    }

    //try to register new user
    fun signUp(login: String, password: String): Boolean {
        val connection = getConnection()!!
        if (connection.from(UserTable).select().where {
                UserTable.name eq login
            }.totalRecords == 1)
            return false
        connection.insert(UserTable) {
            set(it.name, login)
            set(it.psw, password)
            set(it.active, true)
            set(it.lastTimeOfActivity, Instant.now())
        }
        return true
    }
}