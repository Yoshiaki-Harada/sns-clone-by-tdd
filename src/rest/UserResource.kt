package com.harada.rest

import com.google.gson.JsonSyntaxException
import com.harada.Injector
import com.harada.domain.model.user.*
import com.harada.port.UserQueryService
import com.harada.usecase.IUserCreateUseCase
import com.harada.usecase.IUserUpdateUseCase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


fun Application.userModule() {
    userModuleWithDepth(Injector.kodein)
}

fun Application.userModuleWithDepth(kodein: Kodein) {
    // テストの際にここでinstallしないとエラーを起こすため
    if (this.featureOrNull(Locations) == null) install(Locations)
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
    val updateUseCase by kodein.instance<IUserUpdateUseCase>()
    val userQuery by kodein.instance<UserQueryService>()
    routing {
        post("/users") {
            val json = call.receive<RequestUser>()
            val userId =
                createUseCase.execute(User(UserName(json.name), Mail(json.mail), parseDate(json.birthday)))
            call.respond(ResponseUserId(userId.value.toString()))
        }
        @Location("/users")
        data class GetUsersLocation(val name: String? = null, val old_from: Int? = null, val old_to: Int? = null)
        get<GetUsersLocation> { paramas ->
            val name = paramas.name
            val oldFilter = if (paramas.old_from == null && paramas.old_to == null) {
                null
            } else {
                val from = paramas.old_from ?: 0
                val to = paramas.old_to ?: 150
                OldFilter(from, to)
            }
            val users = userQuery.get(
                UserFilter(
                    name?.let { NameFilter(it) },
                    oldFilter
                )
            )

            call.respond(users)
        }
        @Location("/users/{id}")
        data class PutUserLocation(val id: String)
        put<PutUserLocation> { params ->
            val userId = UUID.fromString(params.id)
            val user = call.receive<RequestUpdateUser>()
            updateUseCase.execute(UserId(userId), user = user.toUpdateUser())
            call.respond(emptyMap<String, String>())
        }
    }
}

data class ErrorResponse(val errorMessage: String)

fun parseDate(date: String) = runCatching { SimpleDateFormat("yyyy-MM-dd").parse(date) }.getOrThrow()
data class RequestUser(val name: String, val mail: String, val birthday: String)
data class ResponseUserId(val userId: String)
data class RequestUpdateUser(val name: String?, val mail: String?, val birthday: String?) {
    fun toUpdateUser(
    ) = UpdateUser(
        name = name?.let { UserName(it) },
        birthday = birthday?.let { parseDate(it) },
        mail = mail?.let { Mail(it) }
    )
}
