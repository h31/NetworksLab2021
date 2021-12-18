package auth

import JwtConfig
import collection.UserCollection
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.AuthData
import model.AuthSuccess
import model.User
import org.litote.kmongo.eq

fun Route.authRouting(db: UserCollection) {
    route("/auth") {
        post("/login") {
            val authData = call.receive<AuthData>()
            if (authData.pwdHash.isBlank() || authData.login.isBlank()) return@post call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Password or login is blank.")
            db.getOne(User::login eq authData.login, User::password eq authData.pwdHash) ?: return@post call.respond(
                status = HttpStatusCode.NotFound,
                message = "There is no such username with these login/password.")
            val token = JwtConfig.createToken(authData.login, authData.pwdHash)
            call.respond(
                status = HttpStatusCode.OK,
                message = AuthSuccess(token))
        }
        post ("/register") {
            val authData = call.receive<AuthData>()
            if (authData.pwdHash.isBlank() || authData.login.isBlank()) return@post call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Password or login is blank.")
            if (isValidLogin(authData.login)) return@post call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Login length lesser then 5.")
            if (db.add(User(authData.login, authData.pwdHash))) return@post call.respond(
                status = HttpStatusCode.Created,
                message = "Registration successful. Now you can log in using your credentials at auth/login.")
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Something went wrong. Maybe, this username is already taken?")
        }
    }
}

private fun isValidLogin(login: String): Boolean = login.length < 5