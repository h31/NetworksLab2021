package com.monkeys.terminal.api

import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.CdRequest
import com.monkeys.terminal.models.KillRequest
import com.monkeys.terminal.models.response.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.api(controller: UserController) {
    route("/terminal") {
        authenticate("validate") {
            get("/ls/{location}") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    val result = controller.ls(principal.login, call.parameters["location"] ?: "")
                    if (result == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseModel(
                                "Deleted",
                                OkString("No client with login ${principal.login} in clients list, relogin please"),
                                HttpStatusCode.BadRequest
                            )
                        )
                    } else if (result.isNotEmpty()) {
                        call.respond(HttpStatusCode.OK, OkResponseModel("OK", OkList(result), HttpStatusCode.OK))
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseModel(
                                "Bad Request",
                                OkString("Problems with location to ls"),
                                HttpStatusCode.BadRequest
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = OkString("No token, please signIn"),
                            code = HttpStatusCode.Forbidden
                        )
                    )
                }
            }

            get("/ls") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    val result = controller.ls(principal.login, "")
                    if (result == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseModel(
                                "Deleted",
                                OkString("No client with login ${principal.login} in clients list, relogin please"),
                                HttpStatusCode.BadRequest
                            )
                        )

                    } else if (result.isNotEmpty()) {
                        call.respond(HttpStatusCode.OK, OkResponseModel("OK", OkList(result), HttpStatusCode.OK))
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseModel(
                                "Bad Request",
                                OkString("Problems with location to ls"),
                                HttpStatusCode.BadRequest
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = OkString("No token, please signIn"),
                            code = HttpStatusCode.Forbidden
                        )
                    )
                }
            }

            post("/cd") {
                val principal = call.authentication.principal<AuthModel>()
                val cdRequest = call.receive<CdRequest>()
                if (principal != null) {
                    val result = controller.cd(principal.login, cdRequest)
                    if (result == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseModel(
                                "Deleted",
                                OkString("No client with login ${principal.login} in clients list, relogin please"),
                                HttpStatusCode.BadRequest
                            )
                        )
                    } else if (result != "Error") {
                        call.respond(
                            HttpStatusCode.OK,
                            OkResponseModel(
                                "OK",
                                OkString(result),
                                HttpStatusCode.OK
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponseModel(
                                "Bad Request",
                                OkString("Wrong location to cd"),
                                HttpStatusCode.BadRequest
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = OkString("No token, please signIn"),
                            code = HttpStatusCode.Forbidden
                        )
                    )
                }
            }

            get("/who") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    val res = controller.who()
                    call.respond(
                        HttpStatusCode.OK,
                        OkResponseModel(
                            "OK",
                            OkListOfPairs(res),
                            HttpStatusCode.OK
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = OkString("No token, please signIn"),
                            code = HttpStatusCode.Forbidden
                        )
                    )
                }
            }

            post("/kill") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    if (principal.role.lowercase() == "admin") {
                        val killRequest = call.receive<KillRequest>()
                        controller.kill(killRequest)
                        call.respond(
                            HttpStatusCode.OK,
                            OkResponseModel(
                                "OK",
                                OkString("${killRequest.userToKill} was killed"),
                                HttpStatusCode.OK
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponseModel(
                                message = OkString("You have not enough rights"),
                                code = HttpStatusCode.Forbidden
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = OkString("No token, please signIn"),
                            code = HttpStatusCode.Forbidden
                        )
                    )
                }
            }

            get("/logout") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    controller.logout(principal.login)
                    call.respond(
                        HttpStatusCode.OK,
                        OkResponseModel(
                            "OK",
                            OkString("You was killed (logout successful)"),
                            HttpStatusCode.OK
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = OkString("No token, please signIn"),
                            code = HttpStatusCode.Forbidden
                        )
                    )
                }
            }
        }
    }
}