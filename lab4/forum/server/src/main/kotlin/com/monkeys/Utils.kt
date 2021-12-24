package com.monkeys

import org.ktorm.database.Database
import org.ktorm.schema.*
import java.sql.SQLException

const val user = "postgres"
const val password = "123456"
const val url = "jdbc:postgresql://localhost:5432/forum"

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

object MainThemeTable: Table<Nothing> (schema = "public", tableName = "main_theme") {
    val id = int("id").primaryKey()
    val themeName = varchar("theme_name")
}

object SubThemeTable: Table<Nothing> (schema = "public", tableName = "sub_theme") {
    val id = int("id").primaryKey()
    val themeName = varchar("theme_name")
    val mainThemeId = int("main_theme_id")
}

object MessageTable: Table<Nothing> (schema = "public", tableName = "message") {
    val id = int("id").primaryKey()
    val text = varchar("text")
    val userName = varchar("user_name")
    val time = timestamp("time")
    val subTheme = varchar("sub_theme")
}