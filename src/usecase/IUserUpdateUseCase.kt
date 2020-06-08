package com.harada.usecase

import com.harada.domain.model.user.UpdateUser
import com.harada.domain.model.user.UserId

interface IUserUpdateUseCase {
    fun execute(id: UserId, user : UpdateUser)
}
