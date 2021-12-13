package com.monkeys.terminal

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.FieldNamingPolicy
import com.monkeys.terminal.api.api
import com.monkeys.terminal.auth.AuthRepo
import com.monkeys.terminal.auth.auth
import com.monkeys.terminal.models.AuthModel
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.configure() {

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            serializeNulls()
        }
    }


    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(Authentication) {
        jwt("validate") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256("[/aBJ}S!.c:{u3]"))
                    .withIssuer("pupptmstr")
                    .build()
            )
            validate { credential ->
                val login = credential.payload.getClaim("Login").asString()
                val role = credential.payload.getClaim("Role").asString()
                if (login != null && role != null) {
                    AuthModel(login, "hidden", role)
                } else {
                    null
                }
            }
        }
    }

    val authRepo = AuthRepo()

    routing {
        route("/api/v1") {
            route("") {
                auth(authRepo)
                api()
            }
        }
    }


}
