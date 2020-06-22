package com.harada.domainmodel.tweet

import java.time.ZonedDateTime

data class TweetFilter(
    val text: TextFilter? = null,
    val createTime: TimeFilter? = null
)

data class TextFilter(val value: String)

data class TimeFilter(val from: ZonedDateTime, val to: ZonedDateTime= ZonedDateTime.now())
