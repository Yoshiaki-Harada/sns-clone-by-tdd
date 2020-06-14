package com.harada.rest

import com.harada.Injector
import com.harada.domain.model.message.Text
import com.harada.domain.model.message.Tweet
import com.harada.domain.model.user.UserId
import com.harada.getUUID
import com.harada.usecase.ITweetCreateUseCase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*


@KtorExperimentalLocationsAPI
fun Application.tweetModule() {
    tweetModuleWithDepth(Injector.kodein)
}

@KtorExperimentalLocationsAPI
fun Application.tweetModuleWithDepth(kodein: Kodein) {
    // テストの際にここでinstallしないとエラーを起こすため
    if (this.featureOrNull(Locations) == null) install(Locations)
    val createUseCase by kodein.instance<ITweetCreateUseCase>()
    routing {
        post("/tweets") {
            val json = call.receive<RequestTweet>()
            val userId = createUseCase.execute(
                Tweet(UserId(getUUID(json.userId)), Text(json.text))
            )
            call.respond(ResponseUserId(userId.value.toString()))
        }
    }
}

data class RequestTweet(
    val userId: String,
    val text: String
)