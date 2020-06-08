package com.harada.usecase

import com.harada.domain.model.user.UpdateUser
import com.harada.domain.model.user.UserId
import com.harada.gateway.UserWriteStore

class UserUpdateUseCase(private val store: UserWriteStore) : IUserUpdateUseCase {
    override fun execute(id: UserId, user: UpdateUser) {
        user.mail?.let {
            if (it.isInValid()) throw InvalidMailException(it)
        }
        store.update(id, user)
    }
}
