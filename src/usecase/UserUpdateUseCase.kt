package com.harada.usecase

import com.harada.domain.model.user.UpdateUser
import com.harada.domain.model.user.UserId
import com.harada.gateway.UserNotFoundException
import com.harada.port.UserQueryService
import com.harada.port.UserWriteStore

class UserUpdateUseCase(private val store: UserWriteStore, private val query: UserQueryService) : IUserUpdateUseCase {
    override fun execute(id: UserId, user: UpdateUser) {
        kotlin.runCatching {
            query.get(id)
        }.onFailure {
            throw UserNotFoundException(id.value)
        }

        user.mail?.let {
            if (it.isInValid()) throw InvalidMailException(it)
        }
        store.update(id, user)
    }
}
