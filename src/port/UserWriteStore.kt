package com.harada.port

import com.harada.domain.model.user.UpdateUser
import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId

interface UserWriteStore {
    fun save(user: User): UserId
    fun update(userId: UserId, user:UpdateUser)
}