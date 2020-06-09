package com.harada.usecase

import com.harada.viewmodel.UsersInfo

interface IUsersGetUseCase {
    fun execute() : UsersInfo
}
