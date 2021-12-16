package com.monkeys.terminal.auth

import com.monkeys.terminal.api.UserController
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.response.OkResponseModel
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.auth(authRepo: AuthRepo, userController: UserController) {
    val controller = AuthController(authRepo, userController)

    route("/auth") {
        post("/signin") {
            val res = controller.signIn(call.receive<AuthModel>())
            if (res is OkResponseModel) {
                call.respond(HttpStatusCode.OK, res)
            } else {
                call.respond(HttpStatusCode.BadRequest, res)
            }
        }

        post("/signup") {
            val res = controller.signUp(call.receive<AuthModel>())
            if (res is OkResponseModel) {
                call.respond(HttpStatusCode.Created, res)
            } else {
                call.respond(HttpStatusCode.BadRequest, res)
            }
        }

        authenticate("validate") {
            get("/check-jwt") {
                val principal = call.authentication.principal<AuthModel>()
                call.respond("JWT validated. " +
                        "Login='${principal!!.login}', Password='${principal.password}', Role=${principal.role}")
            }
        }

    }

}