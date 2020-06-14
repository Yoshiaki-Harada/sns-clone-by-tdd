package com.harada.gateway

import com.harada.domain.model.user.UserFilter
import com.harada.domain.model.user.UserId
import com.harada.driver.dao.SqlOldFilter
import com.harada.driver.dao.SqlUserFilter
import com.harada.driver.dao.UserDao
import com.harada.port.UserQueryService
import com.harada.viewmodel.UserInfo
import com.harada.viewmodel.UsersInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class UserQueryPostgresDB(private val dao: UserDao.Companion, private val db: Database) :
    UserQueryService {
    override fun get(filter: UserFilter): UsersInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        val users = dao.get(SqlUserFilter(
            nameFilter = filter.name?.value,
            oldFilter = filter.old?.let {
                val thisYearMonth = LocalDate.now()
                val fromYearMonth = thisYearMonth.minusYears(filter.old.to.toLong())
                val toYearMonth = thisYearMonth.minusYears(filter.old.from.toLong())
                SqlOldFilter(fromYearMonth, toYearMonth)
            }
        ))

        return@transaction UsersInfo(users.map { it.toInfo() })
    }

    override fun get(id: UserId) = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        dao.findById(id.value)?.toInfo() ?: throw UserNotFoundException(userId = id.value)
    }

}

fun UserDao.toInfo() = UserInfo(
    id = id.value.toString(),
    name = name,
    mail = mail,
    birthday = birthday.toString(),
    createdAt = ZonedDateTime.of(createdAt, ZoneId.of("UTC")).toString(),
    updatedAt = ZonedDateTime.of(updatedAt, ZoneId.of("UTC")).toString()
)
