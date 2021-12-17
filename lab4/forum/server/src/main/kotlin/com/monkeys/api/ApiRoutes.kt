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
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val res = controller.getHierarchy(principal.login)
                        if (res.second == "OK") {
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = OkHierarchy(res.first)
                            )
                        } else {
                            enterErrors(call, res.second)
                        }
                    } else {
                        enterJWTError(call)
                    }

                }

                get("/active-users") {
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val res = controller.getActiveUsers(principal.login)
                        if (res.second == "OK") {
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = OKActivityUsers(res.first)
                            )
                        } else {
                            enterErrors(call, res.second)
                        }
                    } else {
                        enterJWTError(call)
                    }
                }

                get("/message") {
                    val principal = call.authentication.principal<AuthModel>()
                    val msg = call.receive<MessageModel>()
                    if (principal != null) {
                        val res = controller.putNewMessage(principal.login, msg)
                        if (res.first) {
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = "Success"
                            )
                        } else {
                            enterErrors(call, principal.login)
                        }
                    } else {
                        enterJWTError(call)
                    }
                }

                get("/message-list") {
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val subTheme = call.receive<ThemeModel>()
                        val res = controller.getMessages(subTheme)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = OkListOfMessage(res)
                        )
                    } else {
                        enterJWTError(call)
                    }
                }

                delete("/logout") {
                    val principal = call.authentication.principal<AuthModel>()
                    if (principal != null) {
                        val res = controller.logout(principal.login)
                        if (res) {
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = "You have successfully logged out"
                            )
                        } else {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = "Something went wrong. Try again"
                            )
                        }
                    } else {
                        enterJWTError(call)
                    }
                }
            }
        }
    }
}

suspend fun enterErrors(call: ApplicationCall, msg: String) {
    if (msg == "You have been inactive for 1 hour. Login again") {
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

suspend fun enterJWTError(call: ApplicationCall) {
    call.respond(
        status = HttpStatusCode.Forbidden,
        message = "No token, please signIn"
    )
}



