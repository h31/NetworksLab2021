package com.monkeys.api

import com.monkeys.controller.UserController
import com.monkeys.models.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.api(controller: UserController) {
    route("/forum") {
        authenticate("validate") {
            route("/request") {

                get("/hierarchy") {
                    try {
                        val principal = call.authentication.principal<AuthModel>()
                        val res = controller.getHierarchy(principal!!.login)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = OkHierarchy(res)
                        )
                    } catch (e: Exception) {
                        sendErrors(call, e.message!!)
                    }
                }

                post("/active-users") {
                    try {
                        val principal = call.authentication.principal<AuthModel>()
                        val res = controller.getActiveUsers(principal!!.login)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = OKActivityUsers(res)
                        )
                    } catch (e: Exception) {
                        sendErrors(call, e.message!!)
                    }
                }

                post("/message") {
                    try {
                        val principal = call.authentication.principal<AuthModel>()
                        val msg = call.receive<MessageModel>()
                        controller.putNewMessage(principal!!.login, msg)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "Success"
                        )
                    } catch (e: Exception) {
                        sendErrors(call, e.message!!)
                    }
                }

                get("/message-list/{sub-theme}") {
                    try {
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
                    } catch (e: Exception) {
                        sendErrors(call, e.message!!)
                    }
                }

                delete("/logout") {
                    try {
                        val principal = call.authentication.principal<AuthModel>()
                        controller.logout(principal!!.login)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "You have successfully logged out"
                        )
                    } catch (e: Exception) {
                        sendErrors(call, e.message!!)
                    }
                }
            }
        }
    }
}

suspend fun sendErrors(call: ApplicationCall, msg: String) {
    if (msg == "You have been inactive for 1 hour. Login again" ||
        msg == "You have been inactive for 1 hour. You have already been logged out"
    ) {
        call.respond(
            status = HttpStatusCode.Unauthorized,
            message = msg
        )
    } else {
        if (msg == "No such sub theme found") {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = msg
            )
        } else {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = msg
            )
        }
    }
}