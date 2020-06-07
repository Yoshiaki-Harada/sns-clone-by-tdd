package com.harada.rest

import com.harada.Injector
import com.harada.domain.model.user.Mail
import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserName
import com.harada.usecase.IUserCreateUseCase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import mu.KotlinLogging
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat


fun Application.userModule() {
    userModuleWithDepth(Injector.kodein)
}

fun Application.userModuleWithDepth(kodein: Kodein) {
    val logger = KotlinLogging.logger {}
    val createUseCase by kodein.instance<IUserCreateUseCase>()
    val sdFormat = SimpleDateFormat("yyyy-MM-dd")

    routing {
        post("/users") {
            val json = call.receive<RequestUser>()
            val userId = createUseCase.execute(User(UserName(json.name), Mail(json.mail), sdFormat.parse(json.birthday)))
            call.respond(ResponseUserId(userId.value.toString()))
        }
    }
}

data class RequestUser(val name: String, val mail: String, val birthday: String)
data class ResponseUserId(val userId: String)
