package com.harada.gateway

import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId

interface UserWriteStore {
    fun save(user: User): UserId
}