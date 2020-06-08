package gateway

import com.harada.driver.dao.UserDao
import com.harada.gateway.UserWritePostgresDB
import createUpdateUser
import createUser
import createUserId
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test

internal class UserWritePostgresDBTest {
    val dao = mockk<UserDao.Companion>(relaxed = true) {
        every { this@mockk.create(any()) } returns createUserId().value
    }

    @Test
    fun `ユーザー情報をDBに保存する`() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        val db = UserWritePostgresDB(dao, mockk())
        db.save(createUser())
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { dao.create(any()) }
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `databaseのユーザー情報を更新する`() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        val db = UserWritePostgresDB(dao, mockk())

        db.update(createUserId(), createUpdateUser(mail = "test@gmail.com"))
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { dao.update(any()) }
    }
}

