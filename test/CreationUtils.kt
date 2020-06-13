import com.harada.domain.model.user.*
import com.harada.driver.entity.UserEntity
import com.harada.driver.entity.UserUpdateEntity
import com.harada.rest.RequestUpdateUser
import com.harada.rest.RequestUser
import com.harada.viewmodel.UserInfo
import com.harada.viewmodel.UsersInfo
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
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

fun createUpdateUser(
    name: String? = null,
    mail: String? = null,
    birthday: String? = null
) = UpdateUser(
    name?.let { UserName(it) },
    mail?.let { Mail(mail) },
    birthday?.let { sdFormat.parse(birthday) }
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

fun createRequestUpdateUser(
    name: String? = null,
    mail: String? = null,
    birthday: String? = null
) = RequestUpdateUser(
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

fun createUpdateUserEntity(
    id: UUID = createUserId().value,
    name: String? = null,
    mail: String? = null,
    birthday: LocalDate? = null,
    updateAt: LocalDateTime = LocalDateTime.of(2020, 1, 1, 1, 10)
) = UserUpdateEntity(
    id = id,
    name = name,
    mail = mail,
    birthday = birthday,
    updatedAt = updateAt
)

fun createUserInfo(
    id: UUID = createUserId().value,
    name: String = createUser().name.toString(),
    mail: String = createUser().mail.toString(),
    birthday: Date = createUser().birthday,
    createdAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")),
    updatedAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC"))
) = UserInfo(
    id = id.toString(),
    name = name,
    mail = mail,
    birthday = sdFormat.format(birthday),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun createUsersInfo(
    userInfoList: List<UserInfo> = listOf(
        createUserInfo(),
        createUserInfo(
            id = UUID.fromString("ebda0d03-71e9-43bc-934b-b335f5708c7e"),
            name = "吉田 次郎",
            mail = "yoshida@gmail.com",
            birthday = sdFormat.parse("1995-01-01")
        )
    )
) = UsersInfo(userInfoList)
