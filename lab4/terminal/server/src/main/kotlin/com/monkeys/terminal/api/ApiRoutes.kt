package com.monkeys.terminal.api

import com.monkeys.terminal.BASE_PATH_HEADER
import com.monkeys.terminal.isClientLogin
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.CdRequest
import com.monkeys.terminal.models.KillRequest
import com.monkeys.terminal.models.response.*
import com.monkeys.terminal.updateUserLastActive
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.api(controller: UserController) {
    route("/terminal") {
        authenticate("validate") {
            post("/ls") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    if (isClientLogin(principal.login)) {
                        val lsRequest = call.receive<CdRequest>()
                        updateUserLastActive(principal.login)
                        val res = controller.ls(lsRequest.basePath, lsRequest.location)
                        if (res != null) {
                            call.respond(HttpStatusCode.OK, OkResponseModel("OK", res))
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponseModel(message = "Wrong path to ls")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponseModel("Logout", "Your session was destroyed")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = "No token, please signIn"
                        )
                    )
                }
            }

            post("/cd") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    if (isClientLogin(principal.login)) {
                        updateUserLastActive(principal.login)
                        val cdRequest = call.receive<CdRequest>()
                        val res = controller.cd(cdRequest.basePath, cdRequest.location)
                        if (res != null) {
                            call.respond(HttpStatusCode.OK, OkResponseModel("OK", res))
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponseModel(message = "Wrong path to cd")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponseModel("Logout", "Your session was destroyed")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = "No token, please signIn"
                        )
                    )
                }
            }

            get("/who") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    if (isClientLogin(principal.login)) {
                        updateUserLastActive(principal.login)
                        val res = controller.who()
                        call.respond(HttpStatusCode.OK, OkResponseModel("OK", res))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponseModel("Logout", "Your session was destroyed")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = "No token, please signIn"
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
                                "${killRequest.userToKill} was killed"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponseModel(
                                message = "You have not enough rights"
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = "No token, please signIn"
                        )
                    )
                }
            }

            get("/logout") {
                val principal = call.authentication.principal<AuthModel>()
                if (principal != null) {
                    if (isClientLogin(principal.login)) {
                        updateUserLastActive(principal.login)
                        controller.logout(principal.login)
                        call.respond(
                            HttpStatusCode.OK,
                            OkResponseModel("Deleted", "Your session was successfully destroyed")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponseModel("Logout", "Your session has already been destroyed")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponseModel(
                            message = "No token, please signIn"
                        )
                    )
                }
            }
        }
    }
}