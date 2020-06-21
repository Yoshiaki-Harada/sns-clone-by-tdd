package com.harada.gateway

import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet
import com.harada.driver.dao.CommentDao
import com.harada.driver.dao.TweetDao
import com.harada.driver.entity.CommentEntity
import com.harada.driver.entity.CommentUpdateEntity
import com.harada.driver.entity.TweetEntity
import com.harada.driver.entity.TweetUpdateEntity
import com.harada.getDateTimeNow
import com.harada.port.TweetNotFoundException
import com.harada.port.TweetWriteStore
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TweetWritePostgresDB(
    private val tweetDao: TweetDao.Companion,
    private val commentDao: CommentDao.Companion,
    private val db: Database
) : TweetWriteStore {
    override fun save(tweet: Tweet): TweetId = transaction(db) {
        tweet.replyTo?.let {
            commentDao.create(
                CommentEntity(
                    id = UUID.randomUUID(),
                    tweetId = it.value,
                    userId = tweet.userId.value,
                    text = tweet.text.value,
                    createdAt = getDateTimeNow(),
                    updatedAt = getDateTimeNow()
                )
            )
        } ?: kotlin.run {
            tweetDao.create(
                TweetEntity(
                    id = UUID.randomUUID(),
                    userId = tweet.userId.value,
                    text = tweet.text.value,
                    createdAt = getDateTimeNow(),
                    updatedAt = getDateTimeNow()
                )
            )
        }
    }.let {
        TweetId(it)
    }

    override fun update(tweetId: TweetId, tweet: UpdateTweet) {
        transaction(db) {
            if (tweetDao.findById(tweetId.value) != null) {
                tweetDao.update(
                    TweetUpdateEntity(
                        tweetId.value,
                        text = tweet.text?.value,
                        updatedAt = getDateTimeNow()
                    )
                )
                return@transaction
            }
            if (commentDao.findById(tweetId.value) != null) {
                commentDao.update(
                    CommentUpdateEntity(
                        tweetId.value,
                        text = tweet.text?.value,
                        updatedAt = getDateTimeNow()
                    )
                )
                return@transaction
            }
            throw TweetNotFoundException(tweetId = tweetId.value)
        }
    }
}

