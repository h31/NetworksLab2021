package com.monkeys.repo

import com.monkeys.DBConnection
import com.monkeys.UserTable
import com.monkeys.getConnection
import com.monkeys.getCurrTimestamp
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
    fun signUp(login: String, password: String): Pair<Boolean, String> {
        return try {
            getConnection()!!.insert(UserTable) {
                set(it.name, login)
                set(it.psw, password)
                set(it.active, true)
                set(it.lastTimeOfActivity, Instant.now())
            }
            Pair(true, "OK")
        } catch (e: SQLException) {
            e.printStackTrace()
            println(e.message)
            Pair(false, e.sqlState)
        }
        //        DBConnection().getConnection().use { connection ->
//            return try {
//                val statement = connection!!.createStatement()
//                statement.execute("INSERT INTO \"user\"(name, psw, active, last_time_of_activity) VALUES ('$login', '$password', 'true', '${getCurrTimestamp()}');")
//                Pair(true, "OK")
//            } catch (e: SQLException) {
//                println("Some problems with registration new user $login")
//                Pair(false, e.sqlState)
//            }
//        }
    }
}