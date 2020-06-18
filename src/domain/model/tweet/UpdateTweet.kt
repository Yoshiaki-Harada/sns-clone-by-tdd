package com.harada.domain.model.message

data class UpdateTweet(val text: Text?) {
    fun isOverLimitTweetLength() = text?.let { !it.isInCharacterLimit() } ?: false
    fun length() = text?.length() ?: 0
}
