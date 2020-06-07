import com.harada.domain.model.user.Mail
import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserId
import com.harada.domain.model.user.UserName
import com.harada.driver.entity.UserEntity
import com.harada.rest.RequestUser
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

val sdFormat = SimpleDateFormat("yyyy-MM-dd")
fun createUser(
    name: String = "Tanaka Taro",
    mail: String = "test@gmail.com",
    birthday: String = "1990-01-01"
) = User(
    UserName(name),
    Mail(mail),
    sdFormat.parse(birthday)
)

fun createUserId(
    id: UUID = UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11")
) = UserId(id)

fun createRequestUser(
    name: String = "Tanaka Taro",
    mail: String = "test@gmail.com",
    birthday: String = "1990-01-01"
) = RequestUser(
    name, mail, birthday
)

fun createUserEntity(
    id: UUID = createUserId().value,
    name: String = "Tanaka Taro",
    mail: String = "test@gmail.com",
    birthday: LocalDate = LocalDate.of(1990, 1, 1),
    createdAt: LocalDateTime = LocalDateTime.now(),
    updateAt: LocalDateTime = LocalDateTime.now()
) = UserEntity(
    id = id,
    name = name,
    mail = mail,
    birthday = birthday,
    createdAt = createdAt,
    updatedAt = updateAt
)