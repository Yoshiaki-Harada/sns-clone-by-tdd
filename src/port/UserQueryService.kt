package com.harada.port

import com.harada.domain.model.user.UserFilter
import com.harada.viewmodel.UsersInfo

interface UserQueryService {
    fun get(filter: UserFilter): UsersInfo
}
