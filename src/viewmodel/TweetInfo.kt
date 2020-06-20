package com.harada.viewmodel

data class TweetInfo(
    val id: String,
    val text: String,
    val userName: String,
    val createdAt: String,
    val updatedAt: String
)

data class TweetsInfo(val tweets: List<TweetInfo>)
