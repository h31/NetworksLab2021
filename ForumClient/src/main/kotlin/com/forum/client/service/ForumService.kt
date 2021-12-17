package com.forum.client.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.forum.client.model.*
import com.forum.client.util.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.time.LocalDateTime

class ForumService(val userName: String, private val password: String) {

    private val domainName = "$PREFIX$HOST$DOUBLE_DOT$PORT"

    companion object {
        private var lastSeenTime = mutableMapOf<String, MutableMap<String, LocalDateTime>>()
        private val jsonType = JSON_TYPE.toMediaTypeOrNull()
    }

    private val client = OkHttpClient.Builder().authenticator { _: Route?, response: Response ->
        val cred = Credentials.basic(userName, password)
        if (cred == response.request.header(AUTHORIZATION)) null
        else response.request.newBuilder().header(AUTHORIZATION, cred).build()
    }.build()

    private val objectMapper = ObjectMapper()

    private fun <T> createAbstractPostRequest(value: T, url: URL): Request {
        val serializeDTO = objectMapper.writeValueAsString(value)
        val requestBody = serializeDTO.toRequestBody(jsonType)
        return Request.Builder().url(url).post(requestBody).build()
    }

    //GET
    fun getAllMessageByTheme(packetMessageDTO: PacketMessageDTO): List<MessageModel>? {
        val url = URL(
            "$domainName$BASE_URL_FORUM$ALL_MESSAGES" + "$PATH_SEP${packetMessageDTO.mainTheme}" +
                    "$PATH_SEP${packetMessageDTO.subTheme}"
        )
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return objectMapper.readValue(bytes, object : TypeReference<List<MessageModel>>() {})
        }
    }

    //POST
    fun sendMessage(messageModelForTransfer: MessageModelDTO): MessageModel? {
        val url = URL("$domainName$BASE_URL_USER$SEND_MESSAGE")
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
        val stringURL = URL(
            "$domainName$BASE_URL_FORUM$NEW_MESSAGE" +
                    "$PATH_SEP${packetMessageDTO.mainTheme}" +
                    "$PATH_SEP${packetMessageDTO.subTheme}"
        )
        val url = stringURL
            .toHttpUrlOrNull()!!
            .newBuilder()
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
        val request = Request.Builder().url(URL("$domainName$BASE_URL_FORUM$ALL_THEMES")).build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return StructureForumModel(objectMapper.readValue(bytes, object : TypeReference<List<MainTheme>>() {}))
        }
    }

    //GET
    fun getActiveUser(): List<ActiveUsers> {
        val request = Request.Builder().url(URL("$domainName$BASE_URL_FORUM$ACTIVE_USERS")).build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body!!.bytes()
            return objectMapper.readValue(bytes, object : TypeReference<List<ActiveUsers>>() {})
        }
    }

    //GET
    fun checkConnection(): Boolean {
        val request = Request.Builder().url(URL("$domainName$BASE_URL_FORUM$ALL_USERS")).build()
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
