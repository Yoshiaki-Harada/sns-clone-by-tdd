package com.harada.driver.entity

import java.time.LocalDateTime
import java.util.*

data class TweetEntity(
    val id: UUID,
    val userId: UUID,
    val text: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
