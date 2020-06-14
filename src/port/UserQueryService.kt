package com.harada.port

import com.harada.domain.model.user.UserFilter
import com.harada.domain.model.user.UserId
import com.harada.viewmodel.UserInfo
import com.harada.viewmodel.UsersInfo

interface UserQueryService {
    fun get(filter: UserFilter): UsersInfo
    fun get(id: UserId): UserInfo
    fun isNotFound(id: UserId): Boolean
}
