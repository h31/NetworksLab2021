package com.monkeys.terminal.api

import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.CdRequest
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*

fun Route.api() {
    route("/terminal") {
        authenticate("validate") {
            get("/ls") {
                val principal = call.authentication.principal<AuthModel>()
                //returns OkResponseModel with OkList inside full of names of directories and files
            }

            post("/cd") {
                val principal = call.authentication.principal<AuthModel>()
                val cdRequest = call.receive<CdRequest>()
                //need cdRequest inside request body
                //returns OkResponseModel with OkString that says new location
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