package com.harada.port

import com.harada.viewmodel.TweetsInfo

interface TweetQueryService {
    fun get(): TweetsInfo
}