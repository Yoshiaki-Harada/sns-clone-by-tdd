package gateway

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.tweet.TextFilter
import com.harada.domain.model.tweet.TimeFilter
import com.harada.domain.model.tweet.TweetFilter
import com.harada.driver.dao.*
import com.harada.formatter
import com.harada.gateway.TweetQueryPostgresDB
import com.harada.viewmodel.TweetsInfo
import createTweetId
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
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
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
        clearMocks(tweetDao, userDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `Tweetをidを指定して取得できる`() {
        every { tweetDao.findById(any<UUID>()) } returns mockTweet
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser

        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk())
        val result = query.get(TweetId(UUID.fromString(createTweetInfo().id)))

        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        assertEquals(result, createTweetInfo())
    }
    @Test
    fun `Tweetをidを指定して存在するかを確認できる`() {
        every { tweetDao.findById(any<UUID>()) } returns null
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser
        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk())
        assertTrue(query.isNotFound(createTweetId()))

        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
    }

    @Test
    fun `Tweetの一覧を取得できる`() {
        every { tweetDao.get(any<SqlTweetFilter>()) } returns listOf(mockTweet)
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser
        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk())
        val result = query.get(TweetFilter())
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


    @Test
    fun `Tweetをテキストで検索できる`() {
        every { tweetDao.get(any<SqlTweetFilter>()) } returns listOf(mockTweet)
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser
        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk())
        query.get(TweetFilter(text = TextFilter("test")))
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify {
            tweetDao.get(SqlTweetFilter(textFilter = "test"))
        }

    }

    @Test
    fun `Tweetを作成時間で検索できる`() {
        every { tweetDao.get(any<SqlTweetFilter>()) } returns listOf(mockTweet)
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser
        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk())
        query.get(
            TweetFilter(
                createTime = TimeFilter(
                    from = ZonedDateTime.parse(
                        "2020-06-17T10:15:30+09:00",
                        formatter
                    ), to = ZonedDateTime.parse(
                        "2020-06-18T10:15:30+09:00",
                        formatter
                    )
                )
            )
        )
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify {
            tweetDao.get(
                SqlTweetFilter(
                    createFilter = SqlDateTimeFilter(
                        from = LocalDateTime.of(2020, 6, 17, 10, 15, 30), to = LocalDateTime.of(2020, 6, 18, 10, 15, 30)
                    )
                )
            )
        }
    }
}