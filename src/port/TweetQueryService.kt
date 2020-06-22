package com.harada.port

import com.harada.domain.model.message.TweetId
import com.harada.domainmodel.tweet.TweetFilter
import com.harada.viewmodel.TweetInfo
import com.harada.viewmodel.TweetsInfo

interface TweetQueryService {
    fun get(id: TweetId): TweetInfo
    fun get(filter: TweetFilter): TweetsInfo
    fun isNotFound(tweetId: TweetId): Boolean
}