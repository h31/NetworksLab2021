package com.monkeys.terminal.auth

import com.monkeys.terminal.api.UserController
import com.monkeys.terminal.models.AuthModel
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.auth(authRepo: AuthRepo, userController: UserController) {
    val controller = AuthController(authRepo, userController)

    route("/auth") {
        post("/signin") {
            call.respond(controller.signIn(call.receive<AuthModel>()))
        }

        post("/signup") {
            call.respond(controller.signUp(call.receive<AuthModel>()))
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