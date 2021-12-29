package com.monkeys.terminal

import java.sql.Timestamp
import java.time.Instant
import java.util.*

const val BASE_PATH_HEADER = "Base-path"


fun isClientLogin(login: String): Boolean {
    DbConnection().getConnection().use { connection ->
        val tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val instantMinusHour = Instant.now().minusMillis(3600000)
        val timestampMinusHour = Timestamp.from(instantMinusHour)
        val ps = connection!!.prepareStatement(
            "SELECT * FROM users WHERE (LOGIN=? AND is_active=true AND (active_last, active_last) OVERLAPS (?, INTERVAL '1 hour'));"
        ).apply {
            setString(1, login)
            setTimestamp(2, timestampMinusHour, tzUTC)
        }

        val resSet =
            ps.executeQuery()
        return resSet.next()
    }
}

fun updateUserLastActive(login: String) {
    DbConnection().getConnection().use { connection ->
        val tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val instant = Instant.now()
        val timestampNow = Timestamp.from(instant)
        val ps = connection!!.prepareStatement("UPDATE users SET active_last=? WHERE LOGIN='$login';")
        ps.setTimestamp(1, timestampNow, tzUTC)
        ps.execute()
    }
}

fun getActiveUsers() : List<String> {
    DbConnection().getConnection().use { connection ->
        val tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val instantMinusHour = Instant.now().minusMillis(3600000)
        val timestampMinusHour = Timestamp.from(instantMinusHour)
        val ps = connection!!.prepareStatement(
            "SELECT * FROM users WHERE (is_active=true AND (active_last, active_last) OVERLAPS (?, INTERVAL '1 hour'));"
        ).apply {
            setTimestamp(1, timestampMinusHour, tzUTC)
        }

        val resSet =
            ps.executeQuery()
        val res = mutableListOf<String>()
        while (resSet.next()) {
            res.add(resSet.getString("login"))
        }
        return res
    }
}

fun killUser(userName: String) {
    DbConnection().getConnection().use { connection ->
        val statement = connection!!.createStatement()
        statement.execute("UPDATE users SET is_active=false WHERE LOGIN='$userName';")
    }
}

fun activateUser(userName: String) {
    DbConnection().getConnection().use { connection ->
        val statement = connection!!.createStatement()
        statement.execute("UPDATE users SET is_active=true WHERE LOGIN='$userName';")
    }
}