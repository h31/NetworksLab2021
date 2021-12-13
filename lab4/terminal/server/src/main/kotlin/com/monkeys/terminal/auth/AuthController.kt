package com.monkeys.terminal.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.Roles
import com.monkeys.terminal.models.response.*
import io.ktor.http.*

class AuthController(private val repo: AuthRepo) {
    fun signIn(model: AuthModel) : ResponseModel {
        return if (repo.signIn(model.login, model.password, model.role)) {
            val jwt = createJwt(model)
            OkResponseModel("OK", AuthOkModel(jwt, "/home/${model.login}/"), HttpStatusCode.OK)
        } else {
            ErrorResponseModel("Error", OkString("Bad credentials"), HttpStatusCode.BadRequest)
        }

    }

    fun signUp(model: AuthModel) : ResponseModel {
        val role = Roles.values().find { userRole -> model.role.lowercase() == userRole.name.lowercase() }
        return if (role != null) {
            if (repo.signUp(model.login, model.password, model.role.lowercase())) {
                OkResponseModel("OK", OkString("Registration done"), HttpStatusCode.OK)
            } else {
                ErrorResponseModel("Error", OkString("Bad credentials"), HttpStatusCode.BadRequest)
            }
        } else {
            ErrorResponseModel(
                "Error",
                OkString("There is no role like that, pls change it to user or admin"),
                HttpStatusCode.BadRequest
            )

        }
    }

    private fun createJwt(model: AuthModel) : String = JWT.create()
        .withSubject("Authentication")
        .withIssuer("pupptmstr")
        .withClaim("Login", model.login)
        .withClaim("Role", model.role)
        .sign(Algorithm.HMAC256("[/aBJ}S!.c:{u3]"))
}