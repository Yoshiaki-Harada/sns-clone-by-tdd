package rest

import com.google.gson.GsonBuilder
import com.harada.domain.model.user.UserId
import com.harada.rest.RequestUser
import com.harada.rest.userModuleWithDepth
import com.harada.usecase.IUserCreateUseCase
import com.harada.usecase.IUserUpdateUseCase
import createRequestUpdateUser
import createRequestUser
import createUpdateUser
import createUser
import createUserId
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
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.util.*

class UserResourceKtTest {

    val gson = GsonBuilder().setPrettyPrinting().create()

    @Nested
    inner class CreateUser {

        val createUseCase = mockk<IUserCreateUseCase>() {
            every { this@mockk.execute(any()) } returns UserId(
                UUID.fromString(
                    "A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"
                )
            )
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

    @Nested
    inner class UpdateUser {
        val updateUseCase = mockk<IUserUpdateUseCase>() {
            every { this@mockk.execute(id = any(), user = any()) } just Runs
        }

        val testKodein = Kodein {
            bind<IUserUpdateUseCase>() with singleton { updateUseCase }
        }

        @Test
        fun `User情報を更新できる`() {
            invokeWithTestUserUpdateApplication(
                testKodein = testKodein,
                body = gson.toJson(createRequestUpdateUser(mail = "update@gmail.com")),
                assert = {
                    assertEquals(HttpStatusCode.OK, response.status())
                    verify {
                        updateUseCase.execute(
                            id = createUserId(),
                            user = createUpdateUser(mail = "update@gmail.com")
                        )
                    }
                }
            )
        }

        @Test
        fun `不正なUserIdの形式のUser情報では更新できない`() {
            invokeWithTestUserUpdateApplication(
                testKodein = testKodein,
                userId = "wrong",
                body = gson.toJson(createRequestUpdateUser(mail = "update@gmail.com")),
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    verify(exactly = 0) {
                        updateUseCase.execute(
                            id = any(),
                            user = any()
                        )
                    }
                }
            )
        }

        fun invokeWithTestUserUpdateApplication(
            testKodein: Kodein,
            body: String?,
            userId: String = createUserId().value.toString(),
            assert: TestApplicationCall.() -> Unit
        ) {
            invokeWithTestApplication(
                moduleFunction = {
                    userModuleWithDepth(testKodein).apply {
                        install(ContentNegotiation) { gson { setPrettyPrinting() } }
                    }
                },
                path = "/users/$userId",
                method = HttpMethod.Put,
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
