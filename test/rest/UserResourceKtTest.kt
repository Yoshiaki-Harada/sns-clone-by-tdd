package rest

import com.google.gson.GsonBuilder
import com.harada.domain.model.user.User
import com.harada.domain.viewmodel.UserId
import com.harada.rest.RequestUser
import com.harada.rest.userModuleWithDepth
import com.harada.usecase.IUserCreateUseCase
import createUser
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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
    @BeforeAll
    fun init() {
        clearMocks(createUseCase)
    }

    @Test
    fun `userを登録することができる`() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val createUseCase = mockk<IUserCreateUseCase>()

        every {
            createUseCase.execute(any<User>())
        } returns UserId(UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))

        val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }

        withTestApplication({
            userModuleWithDepth(testKodein).apply {
                install(ContentNegotiation) {
                    gson {
                        setPrettyPrinting()
                    }
                }
            }
        }) {
            with(handleRequest(HttpMethod.Post, "/users") {
                addHeader(
                    HttpHeaders.ContentType, "${ContentType.Application.Json}"
                )
                setBody(
                    gson.toJson(RequestUser("Tanaka Taro", "test@gmail.com", "1990-01-01"))
                )
            }) {
                verify { createUseCase.execute(createUser()) }
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}