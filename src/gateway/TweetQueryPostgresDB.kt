package com.harada.gateway

import com.harada.driver.dao.TweetDao
import com.harada.driver.dao.UserDao
import com.harada.formatter
import com.harada.port.TweetQueryService
import com.harada.viewmodel.TweetInfo
import com.harada.viewmodel.TweetsInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class TweetQueryPostgresDB(
    private val tweetDao: TweetDao.Companion,
    private val userDao: UserDao.Companion,
    private val db: Database
) : TweetQueryService {
    override fun get(): TweetsInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    )
    {
        tweetDao.get().let {
            it.map { tweet ->
                val user = userDao.findById(tweet.userId) ?: throw UserNotFoundException(tweet.id.value)
                tweet.toInfo(user.name)
            }
        }.let {
            TweetsInfo(it)
        }
    }
}

fun TweetDao.toInfo(userName: String) = TweetInfo(
    id = id.value.toString(),
    text = text,
    userName = userName,
    createdAt = createdAt.format(formatter),
    updatedAt = updatedAt.format(formatter)
)
