package rest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.harada.rest.ResponseUserId
import com.harada.rest.tweetModuleWithDepth
import com.harada.usecase.ITweetCreateUseCase
import createRequestTweet
import createTweet
import createTweetId
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import kotlin.test.assertEquals

class TweetResourceKtTest {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @Nested
    inner class CreateTweet {
        private val useCase = mockk<ITweetCreateUseCase>() {
            every { this@mockk.execute(any()) } returns createTweetId()
        }
        private val testKodein = Kodein {
            bind<ITweetCreateUseCase>() with singleton { useCase }
        }
        @KtorExperimentalLocationsAPI
        @Test
        fun `メッセージを作成する`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "tweets",
                method = HttpMethod.Post,
                body = gson.toJson(createRequestTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweet()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(gson.toJson(ResponseUserId(createTweetId().value.toString())), response.content)
                }
            )
        }

        @Test
        fun `不正なフォーマットのメッセージを作成しようとするとStatusCode 400が返却される`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "tweets",
                method = HttpMethod.Post,
                body = "{\"tex\": \"test\"}",
                contentType = ContentType.Application.Json,
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }
    }
}