package com.harada.usecase

import com.harada.domain.model.user.Mail
import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId
import com.harada.gateway.UserStore

class InvalidMailException(mail: Mail) : Throwable("$mail is invalid format")

class UserCreateUseCase(private val userStore: UserStore) : IUserCreateUseCase {
    override fun execute(user: User): UserId {
        if (user.mail.isInValid()) throw InvalidMailException(user.mail)
        return userStore.save(user)
    }
}
