package com.monkeys.auth

import com.monkeys.controller.AuthController
import com.monkeys.models.AuthModel
import com.monkeys.models.OkAuth
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.auth(controller: AuthController) {

    route("/auth") {
        post("/sign-in") {
            val res = controller.signIn(call.receive<AuthModel>())
            if (res != "Error. Incorrect login or password") {
                call.respond(HttpStatusCode.OK, OkAuth(res))
            } else {
                //User is not found
                call.respond(HttpStatusCode.NotFound, res)
            }
        }

        post("/sign-up") {
            val res = controller.signUp(call.receive<AuthModel>())
            if (res == "Success signup") {
                call.respond(HttpStatusCode.OK, res)
            } else {
                call.respond(HttpStatusCode.BadRequest, res)
            }
        }

        authenticate("validate") {
            get("/check-jwt") {
                val principal = call.authentication.principal<AuthModel>()
                call.respond("JWT validated. " +
                        "Login='${principal!!.login}', Password='${principal.psw}'")
            }
        }

    }

}