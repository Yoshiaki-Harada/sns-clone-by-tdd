package usecase

import com.harada.gateway.UserWriteStore
import com.harada.usecase.InvalidMailException
import com.harada.usecase.UserCreateUseCase
import com.harada.usecase.UserUpdateUseCase
import createUpdateUser
import createUser
import createUserId
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class UserUseCaseTest {
    val store = mockk<UserWriteStore>() {
        every { this@mockk.save(any()) } returns createUserId()
        every { this@mockk.update(any(), any()) } just Runs
    }

    @Test
    fun `ユーザを登録することができる`() {
        val useCase = UserCreateUseCase(store)
        val id = useCase.execute(createUser())
        verify { store.save(createUser()) }
        assertEquals(id, createUserId())
    }

    @Test
    fun `不正なメールアドレスのユーザは登録することができない`() {
        val useCase = UserCreateUseCase(store)
        assertThrows<InvalidMailException> {
            useCase.execute(createUser(mail = "wrong-address"))
        }
        verify(exactly = 0) { store.save(createUser()) }
    }

    @Test
    fun `ユーザを更新することができる`() {
        val useCase = UserUpdateUseCase(store)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        verify { store.update(createUserId(), createUpdateUser(mail = "update@gmail.com")) }
    }

    @Test
    fun `不正なメールアドレスは更新できない`() {
        val useCase = UserUpdateUseCase(store)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        assertThrows<InvalidMailException> {
            useCase.execute(createUserId(), createUpdateUser(mail = "wrong-address"))
        }
        verify(exactly = 0) { store.update(createUserId(), createUpdateUser(mail = "wrong-address")) }
    }

    fun `存在しないユーザは更新できない`() {
        val useCase = UserUpdateUseCase(store)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        verify(exactly = 0) { store.update(createUserId(), createUpdateUser(mail = "wrong-address")) }
    }
}