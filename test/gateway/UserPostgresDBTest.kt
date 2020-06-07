package gateway

import com.harada.driver.dao.UserDao
import com.harada.gateway.UserPostgresDB
import createUser
import createUserId
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test

internal class UserPostgresDBTest {
    @Test
    fun `UserをDBに保存する`() {
        val dao = mockk<UserDao.Companion>(relaxed = true) {
            every { this@mockk.create(any()) } returns createUserId().value
        }
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        val db = UserPostgresDB(dao)
        db.save(createUser())
        verify { transaction(statement = captureLambda<Transaction.() -> Any>()) }
        verify { dao.create(any()) }
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }
}

