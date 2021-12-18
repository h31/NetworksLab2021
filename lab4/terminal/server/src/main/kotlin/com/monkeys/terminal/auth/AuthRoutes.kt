package com.monkeys.terminal.auth

import com.monkeys.terminal.api.UserController
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.response.AuthOkModel
import com.monkeys.terminal.models.response.ErrorResponseModel
import com.monkeys.terminal.models.response.OkResponseModel
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.auth(authRepo: AuthRepo, userController: UserController) {
    val controller = AuthController(authRepo)

    route("/auth") {
        post("/signin") {
            val res = controller.signIn(call.receive<AuthModel>())
            if (res != null) {
                call.respond(HttpStatusCode.OK, OkResponseModel("OK", res))
            } else {
                call.respond(HttpStatusCode.BadRequest, ErrorResponseModel("Bad Credentials", "Wrong login or password"))
            }
        }

        post("/signup") {
           if (controller.signUp(call.receive<AuthModel>())) {
               call.respond(HttpStatusCode.OK)
           } else {
               call.respond(HttpStatusCode.InternalServerError)
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