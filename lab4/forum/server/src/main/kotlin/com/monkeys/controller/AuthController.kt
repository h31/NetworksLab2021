package com.monkeys.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.monkeys.models.AuthModel
import com.monkeys.repo.AuthRepo

class AuthController (private val repo: AuthRepo, private val userController: UserController) {

    //checking user existence
    fun signIn(model: AuthModel) : String {
        return if (repo.signIn(model.login, model.psw)) {
            userController.addUser(model)
            getGenerateToken(model)
        } else {
            "Error. Incorrect login or password"
        }
    }

    //register
    fun signUp(model: AuthModel) : String {
        return if (repo.signUp(model.login, model.psw))
            "Success signup"
        else
            "Something went wrong. Еry to register again"
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