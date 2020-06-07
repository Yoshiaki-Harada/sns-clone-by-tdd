package rest

import com.google.gson.GsonBuilder
import com.harada.domain.model.user.User
import com.harada.domain.viewmodel.UserId
import com.harada.rest.RequestUser
import com.harada.rest.userModuleWithDepth
import com.harada.usecase.IUserCreateUseCase
import createRequestUser
import createUser
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.util.*

internal class UserResourceKtTest {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    val createUseCase = mockk<IUserCreateUseCase>()

    @BeforeAll
    fun init() {
        clearMocks(createUseCase)
    }

    @Test
    fun `userを登録することができる`() {
        every {
            createUseCase.execute(any<User>())
        } returns UserId(UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))

        val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }
        invokeTestApplication(
            moduleFunction = {
                userModuleWithDepth(testKodein).apply {
                    install(ContentNegotiation) { gson { setPrettyPrinting() } }
                }
            },
            path = "/users",
            method = HttpMethod.Post,
            body = gson.toJson(RequestUser("Tanaka Taro", "test@gmail.com", "1990-01-01")),
            contentType = ContentType.Application.Json,
            assert = {
                verify { createUseCase.execute(createUser()) }
                assertEquals(HttpStatusCode.OK, response.status())
            }
        )
    }


    @Test
    fun `不正な日付フォーマットの誕生日のUserを登録するとStatus Code 400が返却される`() {
        every {
            createUseCase.execute(any<User>())
        } returns UserId(UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))

        val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }
        invokeTestApplication(
            moduleFunction = {
                userModuleWithDepth(testKodein).apply {
                    install(ContentNegotiation) { gson { setPrettyPrinting() } }
                }
            },
            path = "/users",
            method = HttpMethod.Post,
            body = gson.toJson(createRequestUser(birthday = "1990-")),
            contentType = ContentType.Application.Json,
            assert = {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        )
    }
    @Test
    fun `不正なフォーマットのUserを登録するとStatus Code 400が返却される`() {
        every {
            createUseCase.execute(any<User>())
        } returns UserId(UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))

        val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }
        invokeTestApplication(
            moduleFunction = {
                userModuleWithDepth(testKodein).apply {
                    install(ContentNegotiation) { gson { setPrettyPrinting() } }
                }
            },
            path = "/users",
            method = HttpMethod.Post,
            body = "{\"nam\" : \"Taro\"",
            contentType = ContentType.Application.Json,
            assert = {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        )
    }
    @Test
    fun `フィールドが揃っていないUserを登録するとStatus Code 400が返却される`() {
        every {
            createUseCase.execute(any<User>())
        } returns UserId(UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))

        val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }
        invokeTestApplication(
            moduleFunction = {
                userModuleWithDepth(testKodein).apply {
                    install(ContentNegotiation) { gson { setPrettyPrinting() } }
                }
            },
            path = "/users",
            method = HttpMethod.Post,
            body = "{\"name\" : \"Taro\"}",
            contentType = ContentType.Application.Json,
            assert = {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        )
    }
}

fun invokeTestApplication(
    moduleFunction: Application.() -> Unit,
    path: String,
    method: HttpMethod,
    body: String?,
    contentType: ContentType,
    assert: TestApplicationCall.() -> Unit
) = withTestApplication(moduleFunction) {
    with(handleRequest(method, path) {
        addHeader(
            HttpHeaders.ContentType, "$contentType"
        )
        if (body != null) setBody(body)
    }) {
        assert.invoke(this)
    }
}
