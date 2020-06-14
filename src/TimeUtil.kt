package com.harada

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
fun getDateTimeNow() = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime()

