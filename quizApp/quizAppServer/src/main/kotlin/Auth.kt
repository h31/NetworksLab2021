import org.ktorm.dsl.*
import java.sql.SQLException

class Auth {
    fun register(login: String, pwdHash: String): Boolean {
        return try {
            connect().insert(UserTable) {
                set(it.login, login)
                set(it.passwordHash, pwdHash)
            }
            true
        } catch (e: SQLException) {
            println(e.message)
            false
        }
    }

    fun login(login: String, pwdHash: String): Boolean  {
        return connect()
            .from(UserTable)
            .select()
            .where { (UserTable.login eq login) and (UserTable.passwordHash eq pwdHash) }
            .totalRecords == 1
    }
}