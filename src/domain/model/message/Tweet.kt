package com.harada.domain.model.message

import com.harada.domain.model.user.UserId

data class Tweet(val userId: UserId, val text: Text)

data class Text(val value: String)
