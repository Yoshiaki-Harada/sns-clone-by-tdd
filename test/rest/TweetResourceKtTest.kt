package rest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.harada.rest.ResponseTweetId
import com.harada.rest.tweetModuleWithDepth
import com.harada.usecase.ITweetCreateUseCase
import com.harada.usecase.ITweetUpdateUseCase
import createRequestTweet
import createRequestUpdateTweet
import createTweet
import createTweetId
import createUpdateTweet
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import kotlin.test.assertEquals

@KtorExperimentalLocationsAPI
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

        @Test
        fun `メッセージを作成する`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets",
                method = HttpMethod.Post,
                body = gson.toJson(createRequestTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweet()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(gson.toJson(ResponseTweetId(createTweetId().value.toString())), response.content)
                }
            )
        }

        @Test
        fun `不正なフォーマットのメッセージを作成しようとするとStatusCode 400が返却される`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets",
                method = HttpMethod.Post,
                body = "{\"tex\": \"test\"}",
                contentType = ContentType.Application.Json,
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }
    }

    @Nested
    inner class UpdateTweet {
        private val useCase = mockk<ITweetUpdateUseCase>() {
            every { this@mockk.execute(any(), any()) } just Runs
        }
        private val testKodein = Kodein {
            bind<ITweetUpdateUseCase>() with singleton { useCase }
        }

        @Test
        fun `メッセージを更新する`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets/${createTweetId().value}",
                method = HttpMethod.Put,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweetId(), createUpdateTweet()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }
        // TODO
        fun `存在しないTweetを更新することはできない`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets/${createTweetId().value}",
                method = HttpMethod.Put,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweetId(), createUpdateTweet()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }
    }
}