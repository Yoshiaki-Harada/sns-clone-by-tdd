package com.harada.domain.model.message

import com.harada.domain.model.user.UserId

const val LIMIT_TWEET_LENGTH = 200

data class Tweet(val userId: UserId, val text: Text) {
    fun isInLimitTweetLength() = text.isInCharacterLimit()
    fun isOverLimitTweetLength() = !text.isInCharacterLimit()
    fun length() = text.length()
}

data class Text(val value: String) {
    fun isInCharacterLimit() = value.length <= LIMIT_TWEET_LENGTH
    fun length() = value.length
}