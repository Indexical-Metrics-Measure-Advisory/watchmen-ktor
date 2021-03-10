import com.imma.model.User
import com.imma.service.Service
import io.ktor.application.*

class LoginService(application: Application) : Service(application) {
    fun login(username: String?, password: String?): User? {
        // TODO do login
        return null
    }
}