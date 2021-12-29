package com.monkeys.service

import com.google.gson.FieldNamingPolicy
import com.monkeys.*
import com.monkeys.models.AuthModel
import com.monkeys.models.CdRequest
import com.monkeys.models.KillRequest
import com.monkeys.models.response.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*

class TerminalService(
    private val name: String,
    private val psw: String
) {

    private var token: String = ""
    private var location: String = ""

    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = GsonSerializer() {
                setPrettyPrinting()
                setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                serializeNulls()
            }
        }
    }

    suspend fun reg(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_UP_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(name, psw)
        }
        if (response.status == HttpStatusCode.OK) {
            return  true
        }
        return false
    }

    suspend fun auth(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_IN_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(name, psw)
        }
        if (response.status == HttpStatusCode.OK) {
            val receive = response.receive<OkResponseModel<AuthOkModel>>()
            token = receive.message.jwt
            location = receive.message.location
            return true
        }
        return false
    }


    //ls
    suspend fun getDirContent(dir: String): List<String> {
        val response = httpClient.post<HttpResponse>(getURL(LS_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = CdRequest(location, dir)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val receive = response.receive<OkResponseModel<List<String>>>()
                return receive.message
            }
            HttpStatusCode.Unauthorized -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                stopClient()
                throw Exception(receive.message)
            }
            else -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                throw Exception(receive.message)
            }
        }
    }

    //cd
    suspend fun getChangeDir(dir: String): String {
        val response = httpClient.post<HttpResponse>(getURL(CD_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = CdRequest(location, dir)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val receive = response.receive<OkResponseModel<String>>()
                location = receive.message
                return receive.message
            }
            HttpStatusCode.Unauthorized -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                stopClient()
                throw Exception(receive.message)
            }
            else -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                throw Exception(receive.message)
            }
        }
    }

    //who
    suspend fun getCurrUsersAndDirs(): List<String> {
        val response = httpClient.get<HttpResponse>(getURL(WHO_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val receive = response.receive<OkResponseModel<List<String>>>()
                return receive.message
            }
            HttpStatusCode.Unauthorized -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                stopClient()
                throw Exception(receive.message)
            }
            else -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                throw Exception(receive.message)
            }
        }
    }

    //kill
    suspend fun kill(user: String): String {
        val response = httpClient.post<HttpResponse>(getURL(KILL_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = KillRequest(user)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val receive = response.receive<OkResponseModel<String>>()
                return receive.message
            }
            HttpStatusCode.Unauthorized -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                stopClient()
                throw Exception(receive.message)
            }
            HttpStatusCode.Forbidden -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                throw Exception(receive.message)
            }
            else -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                throw Exception(receive.message)
            }
        }
    }

    //logout
    suspend fun logout(): String {
        val response = httpClient.get<HttpResponse>(getURL(LOGOUT_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val receive = response.receive<OkResponseModel<String>>()
                stopClient()
                return receive.message
            }
            HttpStatusCode.Unauthorized -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                stopClient()
                throw Exception(receive.message)
            }
            else -> {
                val receive = response.receive<ErrorResponseModel<String>>()
                throw Exception(receive.message)
            }
        }
    }

    fun getClient(): HttpClient = httpClient

    fun getCurrentDir(): String {
        return location
    }

    fun getLogin(): String {
        return name
    }

    private fun stopClient() {
        try {
            httpClient.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}