package com.harada.gateway

import com.harada.viewmodel.UsersInfo

interface UserReadStore {
    fun get(): UsersInfo
}
