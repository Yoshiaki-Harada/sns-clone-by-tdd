package com.harada.gateway

import com.harada.domain.model.message.TweetId
import com.harada.domainmodel.tweet.TweetFilter
import com.harada.driver.dao.*
import com.harada.formatter
import com.harada.port.TweetNotFoundException
import com.harada.port.TweetQueryService
import com.harada.port.UserNotFoundException
import com.harada.viewmodel.ReplyInfo
import com.harada.viewmodel.TimeLine
import com.harada.viewmodel.TweetInfo
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
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
    override fun getTweet(id: TweetId): TweetInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {

        val tweet = tweetDao.findById(id.value) ?: throw TweetNotFoundException(id.value)
        val user = userDao.findById(tweet.userId.value) ?: throw UserNotFoundException(tweet.id.value)
        val tags = tagDao.findTagNamesByTweetId(tweet.id.value)
        val replies = getReply(tweet)
        tweet.toInfo(user.name, tags, replies)
    }

    override fun getTimeLine(filter: TweetFilter): TimeLine = transaction(
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
            },
            tagFilter = filter.tags?.let { SqlTagFilter(it.list) }
        )).let { tweetDaoList ->
            tweetDaoList.sortedByDescending { it.createdAt }.map { tweet ->
                val user = userDao.findById(tweet.userId.value) ?: throw UserNotFoundException(tweet.id.value)
                val tags = tagDao.findTagNamesByTweetId(tweet.id.value)
                val replies = getReply(tweet)
                tweet.toInfo(user.name, tags, replies)
            }
        }.let {
            TimeLine(it)
        }
    }

//    fun getTimeline2(filter: TweetFilter) {
//        getTweetQuery(
//            SqlTweetFilter(
//                textFilter = filter.text?.value,
//                createFilter = filter.createTime?.let {
//                    SqlDateTimeFilter(
//                        it.from.toLocalDateTime(),
//                        it.to.toLocalDateTime()
//                    )
//                },
//                tagFilter = filter.tags?.let { SqlTagFilter(it.list) })
//        ).map { resultRow ->
//            TweetInfo(
//                id = resultRow[Tweets.id].value.toString(),
//                text = resultRow[Tweets.text],
//                userName = "",
//                createdAt = resultRow[Tweets.createdAt].atZone(ZoneId.of("UTC")).format(formatter),
//                updatedAt = resultRow[Tweets.updatedAt].atZone(ZoneId.of("UTC")).format(formatter),
//                replies = emptyList()
//            )
//        }
//    }

    fun getTweetQuery(filter: TweetFilter): Query {
        val sqlFilter = SqlTweetFilter(
            textFilter = filter.text?.value,
            createFilter = filter.createTime?.let {
                SqlDateTimeFilter(
                    it.from.toLocalDateTime(),
                    it.to.toLocalDateTime()
                )
            },
            tagFilter = filter.tags?.let { SqlTagFilter(it.list) }
        )
        if (sqlFilter.tagFilter == null)
            return TweetDao.createTweetFilterCondition(sqlFilter.textFilter, sqlFilter.createFilter)
                ?.let { Tweets.select { it } }
                ?: kotlin.run { return@run Tweets.selectAll() }

        val tagTable = Tags.alias("t")
        val mapTable = TagTweetMap.alias("m")
        val query = Tweets.innerJoin(mapTable, { Tweets.id }, { mapTable[TagTweetMap.tweetId] })
            .innerJoin(tagTable, { tagTable[Tags.id] }, { mapTable[TagTweetMap.tagId] })
            .select { tagTable[Tags.name] inList sqlFilter.tagFilter.tags }.groupBy(Tweets.id)
            .having { Tweets.id.count() eq sqlFilter.tagFilter.tags.size.toLong() }

        return TweetDao.createTweetFilterCondition(sqlFilter.textFilter, sqlFilter.createFilter)
            ?.let { query.andWhere { it } }
            ?: kotlin.run { return@run query }
    }

    fun createTweetFilterCondition(textFilter: String?, createFilter: SqlDateTimeFilter?): Op<Boolean>? =
        when {
            textFilter != null && createFilter != null -> {
                Op.build {
                    Tweets.text like textFilter and Tweets.createdAt.between(
                        createFilter.from,
                        createFilter.to
                    )
                }
            }
            textFilter != null -> {
                Op.build {
                    Tweets.text like "%${textFilter}%"
                }
            }
            createFilter != null -> {
                Op.build {
                    Tweets.createdAt.between(createFilter.from, createFilter.to)
                }
            }
            else -> null
        }

    override fun isNotFound(tweetId: TweetId): Boolean = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        tweetDao.findById(tweetId.value) == null
    }

    private fun getReply(tweet: TweetDao) =
        commentDao.findByTweetId(tweet.id.value).map {
            val replyUser = userDao.findById(it.userId.value) ?: throw UserNotFoundException(tweet.id.value)
            val replyTags = tagDao.findTagNamesByCommentId(it.id.value)
            it.toReplyInfo(replyUser.name, replyTags)
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
