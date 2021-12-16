package com.monkeys

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun getCurrTimestamp(): String = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    .withZone(ZoneOffset.UTC)
    .format(Instant.now())