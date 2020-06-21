package com.harada.gateway

import java.util.*

data class TweetNotFoundException(val tweetId: UUID) : Throwable("Tweet id: $tweetId not found.")
