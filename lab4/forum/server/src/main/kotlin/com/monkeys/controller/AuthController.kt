package com.monkeys.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.monkeys.models.AuthModel
import com.monkeys.repo.AuthRepo
import java.sql.SQLException

class AuthController (private val repo: AuthRepo) {

    //checking user existence
    fun signIn(model: AuthModel) : String {
        return if (repo.signIn(model.login, model.psw)) {
            getGenerateToken(model)
        } else {
            "Error. Incorrect login or password"
        }
    }

    //register
    fun signUp(model: AuthModel): String {
        return try {
            if (repo.signUp(model.login, model.psw))
                return "Success signup"
            "A client with the same name already exists"
        } catch (e: SQLException) {
            "Something went wrong"
        }
    }

    private fun getGenerateToken(user: AuthModel): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer("yana")
            .withClaim("login", user.login)
            .withClaim("psw", user.psw)
            .sign(Algorithm.HMAC256("MySecretAlgorithm"))
    }
}