package com.monkeys.service

import com.google.gson.FieldNamingPolicy
import com.monkeys.*
import com.monkeys.models.CdRequest
import com.monkeys.models.KillRequest
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

class TerminalService(private val name: String,
                      private val psw: String,
                      val role: String) {

    private var token: String = ""
    private var location: String = ""

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
            body = AuthModel(name, psw, role)
        }
        val receive = response.receive<ResponseAuthModel>()
        val message = receive.message
        val msg = message.msg
        token = message.jwt
        location = message.location
        if (msg == "Bad credentials") {
            return false
        }
        return true
    }

    suspend fun auth(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_IN_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(name, psw, role)
        }
        val receive = response.receive<ResponseAuthModel>()
        val message = receive.message
        val msg = message.msg
        token = message.jwt
        location = message.location
        if (msg == "Bad credentials") {
            return false
        }
        return true
    }

    //ls
    suspend fun getDirContent(dir: String): Pair<Boolean, List<String>> {
        val response = httpClient.get<HttpResponse>(getURL(LS_URL + dir)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
        }
        val receive = response.receive<ResponseListOfStringsModel>()
        val message = receive.message
        val msg = message.msg
        return if (response.status.value == 200) {
            val resp = message.response
            Pair(true, resp)
        } else {
            println(msg)
            Pair(false, ArrayList())
        }
    }

    //cd
    suspend fun getChangeDir(dir: String): String {
        val response = httpClient.post<HttpResponse>(getURL(CD_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = CdRequest(dir)
        }
        val receive = response.receive<ResponseStringModel>()
        val message = receive.message
        return message.msg
    }

    //who
    suspend fun getCurrUsersAndDirs(): Pair<Boolean, List<Pair<String, String>>> {
        val response = httpClient.get<HttpResponse>(getURL(WHO_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        val receive = response.receive<ResponseListOfPairsModel>()
        val message = receive.message
        val msg = message.msg
        return if (response.status.value == 200) {
            val resp = message.response
            Pair(true, resp)
        } else {
            println(msg)
            Pair(false, ArrayList())
        }
    }

    //logout
    suspend fun logout(): String {
        val response = httpClient.get<HttpResponse>(getURL(LOGOUT_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
        }
        val receive = response.receive<ResponseStringModel>()
        val message = receive.message
        return message.msg
    }

    //kill
    suspend fun kill(user: String): String {
        val response = httpClient.post<HttpResponse>(getURL(KILL_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            body = KillRequest(user)
        }
        val receive = response.receive<ResponseStringModel>()
        val message = receive.message
        return message.msg
    }

    fun getClient(): HttpClient = httpClient

    fun getCurrentDir(): String {
        return location
    }

    fun getLogin(): String {
        return name
    }

    fun stopClient() {
        try {
            httpClient.close()
        } catch (e: Exception) {

        }
    }
}