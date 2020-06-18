package gateway

import com.harada.driver.dao.TweetDao
import com.harada.gateway.TweetWritePostgresDB
import createTweet
import createTweetId
import createUpdateTweet
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TweetWritePostgresDBTest {

    @BeforeEach
    fun setUp() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `Tweetを作成することができる`() {
        val dao = mockk<TweetDao.Companion>()
        every { dao.create(any()) } returns createTweetId().value
        val db = TweetWritePostgresDB(dao, mockk())
        val tweetId = db.save(createTweet())
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { dao.create(any()) }
        assertEquals(createTweetId(), tweetId)
    }


    @Test
    fun `Tweetを更新することができる`() {
        val dao = mockk<TweetDao.Companion>()
        every { dao.update(any()) } just Runs
        val db = TweetWritePostgresDB(dao, mockk())
        db.update(createTweetId(), createUpdateTweet())
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { dao.update(any()) }
    }
}