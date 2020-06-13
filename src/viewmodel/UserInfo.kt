package com.harada.viewmodel

import java.time.ZonedDateTime

data class UserInfo(
    val id: String,
    val name: String,
    val mail: String,
    val birthday: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
