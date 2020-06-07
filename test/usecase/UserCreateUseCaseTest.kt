package usecase

import com.harada.port.UserStore
import com.harada.usecase.UserCreateUseCase
import createUser
import createUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
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
}