package com.harada.gateway

import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId

interface UserStore {
    fun save(user: User): UserId
}