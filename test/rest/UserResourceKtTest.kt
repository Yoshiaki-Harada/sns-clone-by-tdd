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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.util.*

class UserResourceKtTest {

    @Nested
    inner class CreateUser {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val createUseCase = mockk<IUserCreateUseCase>() {
            every { this@mockk.execute(any()) } returns UserId(UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))
        }

        val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }

        @Test
        fun `userを登録することができる`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = gson.toJson(RequestUser("Tanaka Taro", "test@gmail.com", "1990-01-01")),
                assert = {
                    verify { createUseCase.execute(createUser()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `不正な日付フォーマットの誕生日のUserを登録するとStatus Code 400が返却される`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = gson.toJson(createRequestUser(birthday = "1990-")),
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }

        @Test
        fun `不正なフォーマットのUserを登録するとStatus Code 400が返却される`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = "{\"nam\" : \"Taro\"",
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }

        @Test
        fun `フィールドが揃っていないUserを登録するとStatus Code 400が返却される`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = "{\"name\" : \"Taro\"}",
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }

        fun invokeWithTestUserCreateApplication(
            testKodein: Kodein,
            body: String?,
            assert: TestApplicationCall.() -> Unit
        ) {
            invokeWithTestApplication(
                moduleFunction = {
                    userModuleWithDepth(testKodein).apply {
                        install(ContentNegotiation) { gson { setPrettyPrinting() } }
                    }
                },
                path = "/users",
                method = HttpMethod.Post,
                body = body,
                contentType = ContentType.Application.Json,
                assert = assert
            )
        }
    }
}

fun invokeWithTestApplication(
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
