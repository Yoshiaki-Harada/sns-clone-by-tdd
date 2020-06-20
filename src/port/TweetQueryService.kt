package com.harada.port

import com.harada.domain.model.tweet.TweetFilter
import com.harada.viewmodel.TweetsInfo

interface TweetQueryService {
    fun get(filter: TweetFilter): TweetsInfo
}