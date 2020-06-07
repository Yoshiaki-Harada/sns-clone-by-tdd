package usecase

import com.harada.gateway.UserStore
import com.harada.usecase.InvalidMailException
import com.harada.usecase.UserCreateUseCase
import createUser
import createUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class UserCreateUseCaseTest {
    @Test
    fun `ユーザを登録することができる`() {
        val store = mockk<UserStore>() {
            every { this@mockk.save(any()) } returns createUserId()
        }
        val useCase = UserCreateUseCase(store)
        val id = useCase.execute(createUser())
        verify { store.save(createUser()) }
        assertEquals(id, createUserId())
    }

    @Test
    fun `不正なメールアドレスのユーザは登録することができない`() {
        val store = mockk<UserStore>() {
            every { this@mockk.save(any()) } returns createUserId()
        }
        val useCase = UserCreateUseCase(store)
        assertThrows<InvalidMailException> {
            useCase.execute(createUser(mail = "wrong-address"))
        }
        verify(exactly = 0) { store.save(createUser()) }
    }
}