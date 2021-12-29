package com.monkeys

import com.google.gson.FieldNamingPolicy
import com.monkeys.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*

class ForumService(private val login: String, private val password: String) {

    private var token: String = ""

    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    suspend fun reg(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_UP_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(login, password)
        }
        return if (response.status == HttpStatusCode.OK) {
            val responseBody = response.receive<String>()
            responseBody == "Success signup"
        } else {
            val error = response.receive<String>()
            println(error)
            false
        }
    }

    suspend fun auth(): Boolean {
        val response = httpClient.post<HttpResponse>(getURL(SIGN_IN_URL)) {
            contentType(ContentType.Application.Json)
            body = AuthModel(login, password)
        }
        return if (response.status == HttpStatusCode.OK) {
            val responseBody = response.receive<AuthResponse>()
            token = responseBody.jwt
            token.isNotEmpty()
        } else {
            val error = response.receive<String>()
            println(error)
            false
        }
    }

    suspend fun hierarchy(): Map<String, List<String>>? {
        val response = httpClient.get<HttpResponse>(getURL(HIERARCHY_REQUEST)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
        }
        return if (response.status == HttpStatusCode.OK) {
            response.receive<HierarchyResponse>().hierarchy
        } else {
            val error = response.receive<String>()
            println(error)
            null
        }
    }

    suspend fun activeUsers(): List<String>? {
        val response = httpClient.post<HttpResponse>(getURL(ACTIVE_USERS_REQUEST)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
        }
        return if (response.status == HttpStatusCode.OK) {
            response.receive<ActivityUsersResponse>().users
        } else {
            val error = response.receive<String>()
            println(error)
            null
        }
    }

    suspend fun message(msg: MessageModel) : Boolean {
        val response = httpClient.post<HttpResponse>(getURL(MESSAGE_REQUEST)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
            body = msg
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun messageList(subTheme: String): List<Message>? {
        val response = httpClient.get<HttpResponse>(getURL(MESSAGE_LIST_REQUEST+subTheme)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
        }

        return if (response.status == HttpStatusCode.OK) {
            response.receive<MessageListResponse>().messages
        } else {
            val error = response.receive<String>()
            println(error)
            null
        }
    }

    suspend fun logout() : Boolean {
        val response = httpClient.delete<HttpResponse>(getURL(LOGOUT_REQUEST)) {
            headers {
                append(HttpHeaders.Authorization, TOKEN_PREF + token)
            }
            contentType(ContentType.Application.Json)
        }
        return if (response.status == HttpStatusCode.OK) {
            httpClient.close()
            true
        } else {
            val error = response.receive<String>()
            print(error)
            false
        }

    }

    fun getClient(): HttpClient = httpClient

}