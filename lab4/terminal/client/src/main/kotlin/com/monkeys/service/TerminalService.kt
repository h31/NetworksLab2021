package com.monkeys.service

import com.google.gson.FieldNamingPolicy
import com.monkeys.*
import com.monkeys.models.CdRequest
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.response.AuthOkModel
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

    suspend fun auth(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_UP_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(name, psw, "admin")
        }
        val authOkModel = response.call.receive<AuthOkModel>()
        token = authOkModel.jwt
        location = authOkModel.location
        print("$location> ")
        return true
    }

    suspend fun getDirContent(dir: String): List<String> {
        val response = httpClient.get<HttpResponse>(getURL(LS_URL + dir)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            //body = LsRequest(location)
        }
        return ArrayList<String>()
    }

    fun getCurrentDir(): String {
        return location
    }

    suspend fun getChangeDir(): String {
        val response = httpClient.get<HttpResponse>(getURL(CD_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = CdRequest(location)
        }
        return ""
    }

    suspend fun getCurrUsersAndDirs(): List<Pair<String, String>> {
        val response = httpClient.get<HttpResponse>(getURL(WHO_URL)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            //body = WhoRequest()
        }
        return ArrayList<Pair<String, String>>()
    }

    fun getClient(): HttpClient = httpClient

    fun stopClient() {
        try {
            httpClient.close()
        } catch (e: Exception) {

        }
    }
}