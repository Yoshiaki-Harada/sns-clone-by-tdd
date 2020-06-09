package com.harada.gateway

import com.harada.driver.dao.UserDao
import com.harada.viewmodel.UsersInfo
import org.jetbrains.exposed.sql.Database

class UserReadPostgresDB(val dao: UserDao.Companion, val db: Database) : UserReadStore {
    override fun get(): UsersInfo {
        TODO("Not yet implemented")
    }
}