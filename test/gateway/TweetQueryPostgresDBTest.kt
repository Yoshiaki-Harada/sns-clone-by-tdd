package gateway

import com.harada.driver.dao.TweetDao
import com.harada.driver.dao.Tweets
import com.harada.driver.dao.UserDao
import com.harada.driver.dao.Users
import com.harada.formatter
import com.harada.gateway.TweetQueryPostgresDB
import com.harada.viewmodel.TweetsInfo
import createTweetInfo
import createUserInfo
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class TweetQueryPostgresDBTest {
    private val tweetDao = mockk<TweetDao.Companion>()
    private val userDao = mockk<UserDao.Companion>()
    private val mockTweet = mockk<TweetDao>() {
        every { this@mockk.id } returns EntityID(UUID.fromString(createTweetInfo().id), Tweets)
        every { this@mockk.text } returns createTweetInfo().text
        every { this@mockk.userId } returns EntityID(UUID.randomUUID(), Users)
        every { this@mockk.createdAt } returns LocalDateTime.parse(createTweetInfo().createdAt, formatter)
        every { this@mockk.updatedAt } returns LocalDateTime.parse(createTweetInfo().updatedAt, formatter)
    }

    private val mockUser = mockk<UserDao>() {
        every { this@mockk.name } returns createUserInfo().name
    }

    @BeforeEach
    fun setUp() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(), db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        clearMocks(tweetDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `Tweetの一覧を取得できる`() {
        every { tweetDao.get() } returns listOf(mockTweet)
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser
        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk())
        val result = query.get()
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }

        assertEquals(result, TweetsInfo(listOf(createTweetInfo())))
    }
}