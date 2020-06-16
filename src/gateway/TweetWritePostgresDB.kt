package com.harada.gateway

import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.driver.dao.TweetDao
import com.harada.driver.entity.TweetEntity
import com.harada.getDateTimeNow
import com.harada.port.TweetWriteStore
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TweetWritePostgresDB(val dao: TweetDao.Companion) : TweetWriteStore {
    override fun save(tweet: Tweet): TweetId = transaction {
        dao.create(
            TweetEntity(
                id = UUID.randomUUID(),
                userId = tweet.userId.value,
                text = tweet.text.value,
                createdAt = getDateTimeNow(),
                updatedAt = getDateTimeNow()
            )
        )
    }.let {
        TweetId(it)
    }
}

