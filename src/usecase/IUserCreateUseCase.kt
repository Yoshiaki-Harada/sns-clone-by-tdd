package com.harada.usecase

import com.harada.domain.model.user.User
import com.harada.domain.viewmodel.UserId

interface IUserCreateUseCase {
    fun execute(user: User): UserId
}