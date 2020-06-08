package com.harada

import java.time.ZoneId
import java.time.ZonedDateTime

fun getDateTimeNow() = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime()

