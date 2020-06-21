package com.harada.gateway

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.tweet.TweetFilter
import com.harada.driver.dao.SqlDateTimeFilter
import com.harada.driver.dao.SqlTweetFilter
import com.harada.driver.dao.TweetDao
import com.harada.driver.dao.UserDao
import com.harada.formatter
import com.harada.port.TweetNotFoundException
import com.harada.port.TweetQueryService
import com.harada.port.UserNotFoundException
import com.harada.viewmodel.TweetInfo
import com.harada.viewmodel.TweetsInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.ZoneId

class TweetQueryPostgresDB(
    private val tweetDao: TweetDao.Companion,
    private val userDao: UserDao.Companion,
    private val db: Database
) : TweetQueryService {
    override fun get(id: TweetId): TweetInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        val tweet = tweetDao.findById(id.value) ?: throw TweetNotFoundException(id.value)
        val user = userDao.findById(tweet.userId) ?: throw UserNotFoundException(
            tweet.id.value
        )
        tweet.toInfo(user.name)
    }

    override fun get(filter: TweetFilter): TweetsInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    )
    {
        tweetDao.get(SqlTweetFilter(
            textFilter = filter.text?.value,
            createFilter = filter.createTime?.let {
                SqlDateTimeFilter(
                    it.from.toLocalDateTime(),
                    it.to.toLocalDateTime()
                )
            }
        )).let {
            it.map { tweet ->
                val user = userDao.findById(tweet.userId) ?: throw UserNotFoundException(
                    tweet.id.value
                )
                tweet.toInfo(user.name)
            }
        }.let {
            TweetsInfo(it)
        }
    }

    override fun isNotFound(tweetId: TweetId): Boolean = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        tweetDao.findById(tweetId.value) == null
    }
}

fun TweetDao.toInfo(userName: String) = TweetInfo(
    id = id.value.toString(),
    text = text,
    userName = userName,
    createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
    updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter)
)
