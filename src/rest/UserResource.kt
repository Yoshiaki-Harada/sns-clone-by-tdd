package com.harada.rest

import com.google.gson.JsonSyntaxException
import com.harada.Injector
import com.harada.domain.model.user.Mail
import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserName
import com.harada.usecase.IUserCreateUseCase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.text.ParseException
import java.text.SimpleDateFormat


fun Application.userModule() {
    userModuleWithDepth(Injector.kodein)
}

fun Application.userModuleWithDepth(kodein: Kodein) {
    install(StatusPages) {
        exception<ParseException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<JsonSyntaxException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<IllegalArgumentException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
    }
    val createUseCase by kodein.instance<IUserCreateUseCase>()
    routing {
        post("/users") {
            val json = call.receive<RequestUser>()
            val userId =
                createUseCase.execute(User(UserName(json.name), Mail(json.mail), parseDate(json.birthday)))
            call.respond(ResponseUserId(userId.value.toString()))
        }
    }
}

data class ErrorResponse(val errorMessage: String)

fun parseDate(date: String) = runCatching { SimpleDateFormat("yyyy-MM-dd").parse(date) }.getOrThrow()
data class RequestUser(val name: String, val mail: String, val birthday: String)
data class ResponseUserId(val userId: String)
