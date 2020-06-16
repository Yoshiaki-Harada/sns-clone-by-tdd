package com.harada.driver.dao

import com.harada.driver.entity.TweetEntity
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime
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
    }

    var userId by Tweets.userId
    var text by Tweets.text
    var createdAt by Tweets.createdAt
    var updatedAt by Tweets.updatedAt
}