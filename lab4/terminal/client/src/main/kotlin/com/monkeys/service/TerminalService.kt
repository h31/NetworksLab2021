package com.monkeys.service

import com.google.gson.FieldNamingPolicy
import com.monkeys.*
import com.monkeys.models.CdRequest
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.response.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.lang.Exception

class TerminalService(private val name: String, private val psw: String) {

    private lateinit var token: String
    private lateinit var location: String

    private val httpClient: HttpClient = HttpClient(CIO) {
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
            body = AuthModel(name, psw, "user")
        }
        try {
            val receive = response.receive<OkResponseAuthModel>()
            val message = receive.message
            token = message.jwt
            location = message.location
            return true
        } catch (e: Exception) {
            e.stackTrace
        }
        return false
    }

    suspend fun auth(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_IN_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(name, psw, "user")
        }
        try {
            val receive = response.receive<OkResponseAuthModel>()
            val message = receive.message
            token = message.jwt
            location = message.location
            return true
        } catch (e: Exception) {
            e.stackTrace
        }
        return false
    }

    //ls
    suspend fun getDirContent(dir: String): List<String> {
        val response = httpClient.get<HttpResponse>(getURL(LS_URL + dir)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
        }
        if (response.status.value == 200) {
            try {
                val receive = response.receive<OkResponseListOfStringsModel>()
                val message = receive.message
                return message.msg
            } catch (e: Exception) {
                e.stackTrace
            }
        } else {
            try {
                val receive = response.receive<ErrorResponseListOfStringModel>()
                val message = receive.status
                return ArrayList<String>()
            } catch (e: Exception) {
                e.stackTrace
            }
        }
        return ArrayList<String>()
    }

    fun getCurrentDir(): String {
        return location
    }

    //cd
    suspend fun getChangeDir(dir: String): String {
        val response = httpClient.get<HttpResponse>(getURL(CD_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = CdRequest(dir)
        }
        val status = response.status
        try {
            val receive = response.receive<OkResponseStringModel>()
            val message = receive.message
            return message.msg
        } catch (e: Exception) {
            e.stackTrace
        }
        return ""
    }

    //who
    suspend fun getCurrUsersAndDirs(): List<Pair<String, String>> {
        val response = httpClient.get<HttpResponse>(getURL(WHO_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        try {
            val receive = response.receive<OkResponseListOfPairsModel>()
            val message = receive.message
            return message.msg
        } catch (e: Exception) {
            e.stackTrace
        }
        return ArrayList<Pair<String, String>>()
    }

    //logout
    suspend fun logout(): String {
        val response = httpClient.get<HttpResponse>(getURL(LOGOUT_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        try {
            val receive = response.receive<OkResponseStringModel>()
            val message = receive.message
            return message.msg
        } catch (e: Exception) {
            e.stackTrace
        }
        return ""
    }

    //kill
    suspend fun kill(): String {
        val response = httpClient.get<HttpResponse>(getURL(KILL_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        try {
            val receive = response.receive<OkResponseStringModel>()
            val message = receive.message
            return message.msg
        } catch (e: Exception) {
            e.stackTrace
        }
        return ""
    }

    fun getClient(): HttpClient = httpClient

    fun stopClient() {
        try {
            httpClient.close()
        } catch (e: Exception) {

        }
    }
}