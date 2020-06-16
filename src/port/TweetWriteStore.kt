package com.harada.port

import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId

interface TweetWriteStore {
    fun save(tweet: Tweet): TweetId
}