package com.monkeys

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getCurrTimestamp(): String = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    .withZone(ZoneId.systemDefault())
    .format(Instant.now())