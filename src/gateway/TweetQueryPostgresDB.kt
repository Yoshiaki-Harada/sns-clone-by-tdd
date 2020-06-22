package com.harada.gateway

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.tweet.TweetFilter
import com.harada.driver.dao.*
import com.harada.formatter
import com.harada.port.TweetNotFoundException
import com.harada.port.TweetQueryService
import com.harada.port.UserNotFoundException
import com.harada.viewmodel.ReplyInfo
import com.harada.viewmodel.TweetInfo
import com.harada.viewmodel.TweetsInfo
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.ZoneId

class TweetQueryPostgresDB(
    private val tweetDao: TweetDao.Companion,
    private val userDao: UserDao.Companion,
    private val commentDao: CommentDao.Companion,
    private val db: Database,
    private val tagDao: TagDao.Companion
) : TweetQueryService {
    val logger = KotlinLogging.logger { }
    override fun get(id: TweetId): TweetInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {

        val tweet = tweetDao.findById(id.value) ?: throw TweetNotFoundException(id.value)
        val user = userDao.findById(tweet.userId.value) ?: throw UserNotFoundException(
            tweet.id.value
        )
        val tags = tagDao.findTagNamesByTweetId(tweet.id.value)
        val replies = commentDao.findByTweetId(id.value).map {
            val replyUser = userDao.findById(it.userId.value) ?: throw UserNotFoundException(tweet.id.value)
            val replyTags = tagDao.findTagNamesByCommentId(it.id.value)
            it.toReplyInfo(replyUser.name, replyTags)
        }

        tweet.toInfo(user.name, tags, replies)
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
        )).let { tweetDaoList ->
            tweetDaoList.sortedByDescending { it.createdAt }.map { tweet ->
                val user = userDao.findById(tweet.userId.value) ?: throw UserNotFoundException(
                    tweet.id.value
                )
                val tags = tagDao.findTagNamesByTweetId(tweet.id.value)
                val replies = commentDao.findByTweetId(tweet.id.value).map {
                    val replyUser = userDao.findById(it.userId.value) ?: throw UserNotFoundException(tweet.id.value)
                    val replyTags = tagDao.findTagNamesByCommentId(it.id.value)
                    it.toReplyInfo(replyUser.name, replyTags)
                }
                tweet.toInfo(user.name, tags, replies)
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

fun CommentDao.toReplyInfo(userName: String, tags: List<String>) = ReplyInfo(
    id = id.value.toString(),
    text = text,
    userName = userName,
    createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
    updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter),
    tags = tags
)

fun TweetDao.toInfo(userName: String, tags: List<String>, replies: List<ReplyInfo>) = TweetInfo(
    id = id.value.toString(),
    text = text,
    userName = userName,
    createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
    updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter),
    tags = tags,
    replies = replies
)
