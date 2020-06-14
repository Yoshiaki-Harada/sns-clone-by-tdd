package com.harada.usecase

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.Tweet

interface ITweetCreateUseCase {
    fun execute(tweet: Tweet): TweetId
}