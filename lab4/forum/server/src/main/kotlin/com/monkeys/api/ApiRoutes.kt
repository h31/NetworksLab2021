package com.monkeys.api

import com.monkeys.controller.UserController
import com.monkeys.models.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.sql.SQLException

fun Route.api(controller: UserController) {
    route("/forum") {
        authenticate("validate") {
            route("/request") {

                get("/hierarchy") {
                    val principal = call.authentication.principal<AuthModel>()
                    val res = controller.getHierarchy(principal!!.login)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = OkHierarchy(res)
                    )
                }

                post("/active-users") {
                    val principal = call.authentication.principal<AuthModel>()
                    val res = controller.getActiveUsers(principal!!.login)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = OKActivityUsers(res)
                    )
                }

                post("/message") {
                    val principal = call.authentication.principal<AuthModel>()
                    val msg = call.receive<MessageModel>()
                    controller.putNewMessage(principal!!.login, msg)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = "Success"
                    )
                }

                get("/message-list/{sub-theme}") {
                    val principal = call.authentication.principal<AuthModel>()
                    val subTheme = call.parameters["sub-theme"] ?: call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "No parameters"
                    )
                    val res = controller.getMessages(subTheme.toString(), principal!!.login)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = OkListOfMessage(res)
                    )
                }

                delete("/logout") {
                    val principal = call.authentication.principal<AuthModel>()
                    controller.logout(principal!!.login)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = "You have successfully logged out"
                    )
                }
            }
        }
    }
}
