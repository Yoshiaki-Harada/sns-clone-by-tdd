package com.harada.gateway

import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId
import com.harada.driver.dao.UserDao
import com.harada.driver.entity.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class UserPostgresDB(val dao: UserDao.Companion) : UserStore {
    override fun save(user: User): UserId = transaction {
        dao.create(
            UserEntity(
                UUID.randomUUID(),
                user.name.value,
                user.mail.value,
                LocalDate.ofInstant(user.birthday.toInstant(), ZoneId.of("UTC")),
                ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime(),
                ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime()
            )
        ).let {
            UserId(it)
        }
    }
}