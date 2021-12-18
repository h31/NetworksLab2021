package com.forum.client.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.forum.client.model.*
import com.forum.client.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime

class ForumService(val userName: String, private val password: String) {

    private val lastSeenTime = mutableMapOf<String, MutableMap<String, LocalDateTime>>()
    private val jsonType = JSON_TYPE.toMediaTypeOrNull()
    private val objectMapper = ObjectMapper()

    private val client = OkHttpClient.Builder().authenticator { _: Route?, response: Response ->
        val cred = Credentials.basic(userName, password)
        if (cred == response.request.header(AUTHORIZATION)) null
        else response.request.newBuilder().header(AUTHORIZATION, cred).build()
    }.build()

    private fun <T> createAbstractPostRequest(value: T, url: HttpUrl): Request {
        val serializeDTO = objectMapper.writeValueAsString(value)
        val requestBody = serializeDTO.toRequestBody(jsonType)
        return Request.Builder().url(url).post(requestBody).build()
    }

    private fun buildDomainURL(baseURL: String, endPoint: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(PREFIX)
            .host(HOST)
            .port(PORT)
            .addPathSegment(baseURL)
            .addPathSegment(endPoint)
            .build()
    }

    //GET
    fun getAllMessageByTheme(packetMessageDTO: PacketMessageDTO): List<MessageModel>? {
        val url = buildDomainURL(BASE_URL_FORUM, ALL_MESSAGES)
            .newBuilder()
            .addPathSegment(packetMessageDTO.mainTheme)
            .addPathSegment(packetMessageDTO.subTheme)
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return objectMapper.readValue(bytes, object : TypeReference<List<MessageModel>>() {})
        }
    }

    //POST
    fun sendMessage(messageModelForTransfer: MessageModelDTO): MessageModel? {
        val url = buildDomainURL(BASE_URL_USER, SEND_MESSAGE)
        val request = createAbstractPostRequest(messageModelForTransfer, url)
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return objectMapper.readValue(bytes, MessageModel::class.java)
        }
    }

    //GET
    fun getNewMessageByTheme(packetMessageDTO: PacketMessageDTO): List<MessageModel>? {
        val lastSeenTimeLocal: LocalDateTime? = getActualLastSeenDateTime(packetMessageDTO)
        packetMessageDTO.lastSeenTime = lastSeenTimeLocal?.toString() ?: LocalDateTime.MIN.toString()
        val url = buildDomainURL(BASE_URL_FORUM, NEW_MESSAGE)
            .newBuilder()
            .addPathSegment(packetMessageDTO.mainTheme)
            .addPathSegment(packetMessageDTO.subTheme)
            .addQueryParameter("time", packetMessageDTO.lastSeenTime)
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            setupLastSeenDateTime(packetMessageDTO)
            return objectMapper.readValue(response.body!!.bytes(), object : TypeReference<List<MessageModel>>() {})
        }
    }

    //GET
    fun getAllThemes(): StructureForumModel {
        val url = buildDomainURL(BASE_URL_FORUM, ALL_THEMES)
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return StructureForumModel(objectMapper.readValue(bytes, object : TypeReference<List<MainTheme>>() {}))
        }
    }

    //GET
    fun getActiveUser(): List<ActiveUsers> {
        val url = buildDomainURL(BASE_URL_FORUM, ACTIVE_USERS)
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return objectMapper.readValue(bytes, object : TypeReference<List<ActiveUsers>>() {})
        }
    }

    //GET
    fun checkConnection(): Boolean {
        val url = buildDomainURL(BASE_URL_FORUM, ALL_USERS)
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { return it.code == 200 }
    }


    private fun getActualLastSeenDateTime(packetMessageDTO: PacketMessageDTO): LocalDateTime? {
        return if (lastSeenTime.containsKey(packetMessageDTO.mainTheme)) {
            if (lastSeenTime[packetMessageDTO.mainTheme]!!.containsKey(packetMessageDTO.subTheme)) {
                lastSeenTime[packetMessageDTO.mainTheme]!![packetMessageDTO.subTheme]
            } else null
        } else null
    }

    private fun setupLastSeenDateTime(packetMessageDTO: PacketMessageDTO) {
        if (getActualLastSeenDateTime(packetMessageDTO) != null) {
            lastSeenTime[packetMessageDTO.mainTheme]!![packetMessageDTO.subTheme] = LocalDateTime.now()
        } else {
            lastSeenTime[packetMessageDTO.mainTheme] = mutableMapOf(packetMessageDTO.subTheme to LocalDateTime.now())
        }
    }
}
