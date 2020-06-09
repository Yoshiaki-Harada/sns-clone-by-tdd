package com.harada.usecase

import com.harada.gateway.UserReadStore
import com.harada.viewmodel.UsersInfo

class UsersGetUseCase(private val store: UserReadStore): IUsersGetUseCase {
    override fun execute(): UsersInfo = store.get()
}