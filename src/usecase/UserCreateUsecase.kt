package com.harada.usecase

import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId
import com.harada.port.UserStore

class UserCreateUseCase(private val userStore: UserStore) : IUserCreateUseCase {
    override fun execute(user: User): UserId {
        TODO("Not yet implemented")
    }
}
