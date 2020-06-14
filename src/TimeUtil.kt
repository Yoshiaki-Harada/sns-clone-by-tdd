package com.harada

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
fun getDateTimeNow(): LocalDateTime = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime()

