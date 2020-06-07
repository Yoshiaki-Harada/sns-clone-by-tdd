package com.harada.port

import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId

interface UserStore {
    fun save(user: User): UserId
}