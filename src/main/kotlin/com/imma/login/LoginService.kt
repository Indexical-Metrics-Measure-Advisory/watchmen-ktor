import com.imma.model.User
import com.imma.service.Service
import com.imma.user.UserService
import io.ktor.application.*

class LoginService(application: Application) : Service(application) {
    fun login(username: String?, password: String?): User? {
        // TODO do login
        if (username == null || username.isBlank()) {
            return null
        }

        val user = UserService(application).findUserByName(username)

        return user
    }
}