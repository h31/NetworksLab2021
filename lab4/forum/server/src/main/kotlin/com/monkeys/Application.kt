package com.monkeys

import api
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.monkeys.auth.auth
import com.monkeys.controller.AuthController
import com.monkeys.controller.UserController
import io.ktor.server.netty.*
import com.monkeys.repo.AuthRepo
import com.monkeys.models.AuthModel
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.configure() {

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("validate") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256("MySecretAlgorithm"))
                    .withIssuer("yana")
                    .build()
            )
            validate { credential ->
                val login = credential.payload.getClaim("login").asString()
                val psw = credential.payload.getClaim("psw").asString()
                if (login != null && psw != null) {
                    AuthModel(login, psw)
                } else {
                    null
                }
            }
        }
    }

    val authRepo = AuthRepo()
    val userController = UserController()

    routing {
        auth(AuthController(authRepo, userController))
        api(userController)
    }
}
