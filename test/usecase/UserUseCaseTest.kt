package usecase

import com.harada.domain.model.user.UserFilter
import com.harada.port.UserQueryService
import com.harada.port.UserWriteStore
import com.harada.usecase.InvalidMailException
import com.harada.usecase.UserCreateUseCase
import com.harada.usecase.UserUpdateUseCase
import createUpdateUser
import createUser
import createUserId
import createUsersInfo
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class UserUseCaseTest {
    val writeStore = mockk<UserWriteStore>() {
        every { this@mockk.save(any()) } returns createUserId()
        every { this@mockk.update(any(), any()) } just Runs
    }

    @Test
    fun `ユーザを登録することができる`() {
        val useCase = UserCreateUseCase(this.writeStore)
        val id = useCase.execute(createUser())
        verify { this@UserUseCaseTest.writeStore.save(createUser()) }
        assertEquals(id, createUserId())
    }

    @Test
    fun `不正なメールアドレスのユーザは登録することができない`() {
        val useCase = UserCreateUseCase(this.writeStore)
        assertThrows<InvalidMailException> {
            useCase.execute(createUser(mail = "wrong-address"))
        }
        verify(exactly = 0) { this@UserUseCaseTest.writeStore.save(createUser()) }
    }

    @Test
    fun `ユーザを更新することができる`() {
        val useCase = UserUpdateUseCase(this.writeStore)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        verify { this@UserUseCaseTest.writeStore.update(createUserId(), createUpdateUser(mail = "update@gmail.com")) }
    }

    @Test
    fun `不正なメールアドレスは更新できない`() {
        val useCase = UserUpdateUseCase(this.writeStore)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        assertThrows<InvalidMailException> {
            useCase.execute(createUserId(), createUpdateUser(mail = "wrong-address"))
        }
        verify(exactly = 0) {
            this@UserUseCaseTest.writeStore.update(
                createUserId(),
                createUpdateUser(mail = "wrong-address")
            )
        }
    }

    fun `存在しないユーザは更新できない`() {
        val useCase = UserUpdateUseCase(this.writeStore)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        verify(exactly = 0) {
            this@UserUseCaseTest.writeStore.update(
                createUserId(),
                createUpdateUser(mail = "wrong-address")
            )
        }
    }
}