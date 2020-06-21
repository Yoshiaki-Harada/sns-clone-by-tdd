package gateway

import com.harada.driver.dao.CommentDao
import com.harada.driver.dao.TweetDao
import com.harada.gateway.TweetWritePostgresDB
import com.harada.port.TweetNotFoundException
import createTweet
import createTweetId
import createUpdateTweet
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class TweetWritePostgresDBTest {
    private val tweetDao = mockk<TweetDao.Companion>()
    private val commentDao = mockk<CommentDao.Companion>()

    @BeforeEach
    fun setUp() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        clearMocks(tweetDao, commentDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `Tweetを作成することができる`() {
        val tweetDao = mockk<TweetDao.Companion>()
        every { tweetDao.create(any()) } returns createTweetId().value
        val db = TweetWritePostgresDB(tweetDao, mockk(), mockk())
        val tweetId = db.save(createTweet())

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { tweetDao.create(any()) }
        assertEquals(createTweetId(), tweetId)
    }

    @Test
    fun `Tweetに対してリプライできる`() {
        val commentDao = mockk<CommentDao.Companion>()
        every { commentDao.create(any()) } returns createTweetId().value
        val db = TweetWritePostgresDB(mockk(), commentDao, mockk())
        val tweetId = db.save(createTweet(replyTo = UUID.fromString("7865abd1-886d-467d-ac59-7df75d010473")))

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { commentDao.create(any()) }
        assertEquals(tweetId, createTweetId())
    }

    @Test
    fun `Tweetを更新することができる`() {
        every { tweetDao.findById(any<UUID>()) } returns mockk()
        every { tweetDao.update(any()) } just Runs
        val db = TweetWritePostgresDB(tweetDao, mockk(), mockk())
        db.update(createTweetId(), createUpdateTweet())

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { tweetDao.update(any()) }
    }

    @Test
    fun `Tweet(リプライ)を更新することができる`() {
        every { tweetDao.findById(any<UUID>()) } returns null
        every { commentDao.findById(any<UUID>()) } returns mockk()
        every { commentDao.update(any()) } just Runs
        val db = TweetWritePostgresDB(tweetDao, commentDao, mockk())
        db.update(createTweetId(), createUpdateTweet())

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { commentDao.update(any()) }
    }

    @Test
    fun `存在しないツイートは更新することができない`() {
        every { tweetDao.findById(any<UUID>()) } returns null
        every { commentDao.findById(any<UUID>()) } returns null
        val db = TweetWritePostgresDB(tweetDao, commentDao, mockk())
        assertThrows<TweetNotFoundException> {
            db.update(createTweetId(), createUpdateTweet())
        }
    }
}