package com.harada.rest

import com.harada.Injector
import com.harada.domain.model.message.Text
import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet
import com.harada.domain.model.user.UserId
import com.harada.getUUID
import com.harada.port.TweetQueryService
import com.harada.usecase.ITweetCreateUseCase
import com.harada.usecase.ITweetUpdateUseCase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.kodein.di.Kodein
import org.kodein.di.generic.instance


@KtorExperimentalLocationsAPI
fun Application.tweetModule() {
    tweetModuleWithDepth(Injector.kodein)
}

@KtorExperimentalLocationsAPI
fun Application.tweetModuleWithDepth(kodein: Kodein) {
    // テストの際にここでinstallしないとエラーを起こすため
    if (this.featureOrNull(Locations) == null) install(Locations)
    val createUseCase by kodein.instance<ITweetCreateUseCase>()
    val updateUseCase by kodein.instance<ITweetUpdateUseCase>()
    val query by kodein.instance<TweetQueryService>()
    routing {
        post("/tweets") {
            val json = call.receive<RequestTweet>()
            val userId = createUseCase.execute(
                Tweet(UserId(getUUID(json.userId)), Text(json.text))
            )
            call.respond(ResponseTweetId(userId.value.toString()))
        }
        @Location("/tweets/{id}")
        data class PutTweetLocation(val id: String)
        put<PutTweetLocation> { params ->
            val tweetId = getUUID(params.id)
            val tweet = call.receive<RequestUpdateTweet>()
            updateUseCase.execute(
                tweetId = TweetId(tweetId),
                tweet = UpdateTweet(
                    tweet.text?.let { Text(it) }
                )
            )
            call.respond(emptyMap<String, String>())
        }
        get("/tweets") {
            call.respond(query.get())
        }
    }
}

data class RequestUpdateTweet(val text: String?)

data class RequestTweet(
    val userId: String,
    val text: String
)

data class ResponseTweetId(
    val tweetId: String
)