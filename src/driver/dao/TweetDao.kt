package com.harada.driver.dao

import com.harada.driver.entity.TweetEntity
import com.harada.driver.entity.TweetUpdateEntity
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.and
import java.time.LocalDateTime
import java.util.*

object Tweets : UUIDTable() {
    val userId = reference("user_id", Users)
    val text = varchar("text", 500)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class TweetDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TweetDao>(Tweets) {
        fun create(entity: TweetEntity) = TweetDao.new(entity.id) {
            userId = EntityID(entity.userId, Tweets)
            text = entity.text
            createdAt = entity.createdAt
            updatedAt = entity.updatedAt
        }.id.value

        fun update(entity: TweetUpdateEntity) {
            TweetDao.findById(entity.id)?.let { tweetDao ->
                entity.text?.let {
                    tweetDao.text = it
                }
                tweetDao.updatedAt = entity.updatedAt
            }
        }

        fun get(filter: SqlTweetFilter): List<TweetDao> {
            val cond = when {
                filter.textFilter != null && filter.createFilter != null -> {
                    Op.build {
                        Tweets.text like filter.textFilter and Tweets.createdAt.between(
                            filter.createFilter.from,
                            filter.createFilter.to
                        )
                    }
                }
                filter.textFilter != null -> {
                    Op.build {
                        Tweets.text like "%${filter.textFilter}%"
                    }
                }
                filter.createFilter != null -> {
                    Op.build {
                        Tweets.createdAt.between(filter.createFilter.from, filter.createFilter.to)
                    }
                }
                else -> null
            }
            return cond?.let { TweetDao.find(it).toList() } ?: TweetDao.all().toList()
        }
    }

    var userId by Tweets.userId
    var text by Tweets.text
    var createdAt by Tweets.createdAt
    var updatedAt by Tweets.updatedAt
}

data class SqlTweetFilter(val textFilter: String? = null, val createFilter: SqlDateTimeFilter? = null)

data class SqlDateTimeFilter(val from: LocalDateTime, val to: LocalDateTime)