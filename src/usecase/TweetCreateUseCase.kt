package com.harada.usecase

import com.harada.domain.model.message.LIMIT_TWEET_LENGTH
import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet
import com.harada.gateway.UserNotFoundException
import com.harada.port.TweetWriteStore
import com.harada.port.UserQueryService

data class OverTweetLengthException(val length: Int) :
    Throwable("Tweet Length must be in $LIMIT_TWEET_LENGTH. but actually $length")

class TweetCreateUseCase(private val store: TweetWriteStore, private val query: UserQueryService) :
    ITweetCreateUseCase {
    override fun execute(tweet: Tweet): TweetId {
        if (query.isNotFound(tweet.userId)) throw UserNotFoundException(tweet.userId.value)
        if (tweet.isOverLimitTweetLength()) throw OverTweetLengthException(tweet.length())
        return store.save(tweet)
    }
}