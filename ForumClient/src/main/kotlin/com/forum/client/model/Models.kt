package com.forum.client.model

import com.forum.client.util.VOID

data class StructureForumModel(
    val mainThemeList: List<MainTheme>
)

data class MainTheme(
    val name: String = VOID,
    val subThemeList: List<SubThemes> = listOf()
)

data class SubThemes(
    val name: String = VOID,
    val messageModelList: List<MessageModel> = listOf()
)

open class MessageModel(
    val userName: String = VOID,
    val message: String = VOID,
    val dateTime: String = VOID
)

class MessageModelDTO(
    userName: String = VOID,
    message: String = VOID,
    val mainThemeName: String = VOID,
    val subThemeName: String = VOID
) : MessageModel(userName, message)

data class PacketMessageDTO(
    val mainTheme: String,
    val subTheme: String,
    var lastSeenTime: String = VOID
)

class UserModel(val userName: String = VOID)

class ActiveUsers(
    val userModel: UserModel = UserModel(),
    val lastAction: String = VOID
)


