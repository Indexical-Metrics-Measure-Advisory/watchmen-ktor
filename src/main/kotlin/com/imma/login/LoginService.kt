import com.imma.auth.adminEnabled
import com.imma.auth.adminPassword
import com.imma.auth.adminUsername
import com.imma.model.User
import com.imma.service.Service
import com.imma.user.UserCredentialService
import com.imma.user.UserService
import com.imma.utils.getCurrentDateTime
import io.ktor.application.*
import org.mindrot.jbcrypt.BCrypt

class LoginService(application: Application) : Service(application) {
    fun login(username: String?, plainPassword: String?): User? {
        if (username == null || username.isBlank()) {
            return null
        }

        if (application.adminEnabled
            && username == application.adminUsername
            && plainPassword == application.adminPassword
        ) {
            return User().apply {
                val now = getCurrentDateTime()
                userId = username
                name = username
                nickName = username
                active = true
                createTime = now
                lastModifyTime = now
            }
        }

        val user = UserService(application).findUserByName(username)
        val credential = UserCredentialService(application).findCredentialByName(username) ?: return null

        val hashedPassword: String = credential.credential!!
        return if (BCrypt.checkpw(plainPassword, hashedPassword)) {
            user
        } else {
            null
        }
    }
}