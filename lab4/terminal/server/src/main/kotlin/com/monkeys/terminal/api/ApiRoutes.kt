package com.monkeys.terminal.api

import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.CdRequest
import com.monkeys.terminal.models.response.ErrorResponseModel
import com.monkeys.terminal.models.response.OkList
import com.monkeys.terminal.models.response.OkResponseModel
import com.monkeys.terminal.models.response.OkString
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
                    if (result != null) {
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
                    if (result != "Error") {
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
                //returns OkResponseModel with OkList inside full of names and locations for every user
            }

            post("/kill") {
                val principal = call.authentication.principal<AuthModel>()
                //need killRequest inside request body
                //if u'r admin - returns result in OkResponseModel inside that is OkString else error with code not enough rights
            }

            get("/logout") {
                val principal = call.authentication.principal<AuthModel>()
                //returns OkResponseModel with OkString that says that u'r logged out
            }
        }
    }
}