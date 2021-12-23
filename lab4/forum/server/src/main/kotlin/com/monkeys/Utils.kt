package com.monkeys

import org.ktorm.database.Database
import org.ktorm.schema.*
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val user = "postgres"
const val password = "123456"
const val url = "jdbc:postgresql://localhost:5432/forum"

fun getCurrTimestamp(): Timestamp = Timestamp.valueOf(DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    .withZone(ZoneId.systemDefault())
    .format(Instant.now()))

fun getConnection(): Database? {
    try {
        return Database.connect(url, user = user, password = password)
    } catch (e: SQLException) {
        println("Error while connecting to DB")
        e.printStackTrace()
    }
    return null
}

object UserTable: Table<Nothing> (schema = "public", tableName = "user") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val psw = varchar("psw")
    val active = boolean("active")
    val lastTimeOfActivity = timestamp("last_time_of_activity")
}