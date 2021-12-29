package com.monkeys.auth

import com.monkeys.controller.AuthController
import com.monkeys.models.AuthModel
import com.monkeys.models.OkAuth
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.auth(controller: AuthController) {

    route("/auth") {
        post("/sign-in") {
            val res = controller.signIn(call.receive<AuthModel>())
            if (res != "Error. Incorrect login or password") {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = OkAuth(res)
                )
            } else {
                //User is not found
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = res)
            }
        }

        post("/sign-up") {
            val res = controller.signUp(call.receive<AuthModel>())
            if (res == "Success signup") {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = res)
            } else {
                if (res == "Something went wrong. Try to register again")
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = res)
                else
                    //prohibited in client creation
                    call.respond(
                        status = Forbidden,
                        message = res)
            }
        }

    }

}