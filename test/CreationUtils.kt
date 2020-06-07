import com.harada.domain.model.user.Mail
import com.harada.domain.model.user.User
import com.harada.domain.model.user.UserName
import java.text.SimpleDateFormat

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
