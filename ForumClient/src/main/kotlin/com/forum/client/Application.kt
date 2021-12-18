package com.forum.client

import com.forum.client.model.MessageModelDTO
import com.forum.client.model.PacketMessageDTO
import com.forum.client.model.StructureForumModel
import com.forum.client.service.ForumService
import com.forum.client.util.DOT
import com.forum.client.util.VOID
import java.util.*
import kotlin.system.exitProcess

class Application {

    private var conditionLevelFirst = true
    private var conditionLevelSecond = true
    private var conditionLevelThird = true

    fun startClient() {
        val scanner = Scanner(System.`in`)
        println("Enter your login:")
        val userName = scanner.nextLine()
        println("Enter your password:")
        val password = scanner.nextLine()
        val forumService = ForumService(userName, password)
        if (!forumService.checkConnection()) {
            println("NE PROYDESH', TI NE ODNA IZ NAS!")
            println("Incorrect login or password")
            exitProcess(42)
        } else menuLevelFirst(forumService, scanner)
    }

    private fun menuLevelFirst(forumService: ForumService, scanner: Scanner) {
        println("!!!Welcome to forum for young mums!!!")
        println("[1] - Check forum tree [2] - Check active users [3] - Exit")
        conditionLevelFirst = true
        while (conditionLevelFirst) actionLevelFirst(forumService, scanner)
    }

    private fun actionLevelFirst(forumService: ForumService, scanner: Scanner) {
        when (scanner.nextLine()) {
            "1" -> menuLevelSecond(forumService, scanner)
            "2" -> {
                val listUser = forumService.getActiveUser()
                for (user in listUser) {
                    println("Name: ${user.userModel.userName} | Last action: ${user.lastAction}")
                }
                println("[1] - Check forum tree [2] - Check active users [3] - Exit")
            }
            "3" -> conditionLevelFirst = false
            else -> println("Tak nel'zy'a!!! :)))))))), try again!")
        }
    }

    private fun menuLevelSecond(forumService: ForumService, scanner: Scanner) {
        val structureForumModel = forumService.getAllThemes()
        printForumTree(structureForumModel)
        println("Select necessary themes and sub themes (for example: 1.3)")
        println("[any number] - select theme [back] - Back")
        conditionLevelSecond = true
        while (conditionLevelSecond) {
            val input = scanner.nextLine()
            val packetMessageDTO = getPacketMessageOrNull(input, structureForumModel)
            if (input == "back") {
                conditionLevelSecond = false
                println("[1] - Check forum tree [2] - Check active users [3] - Exit")
            }
            else if (packetMessageDTO == null) {
                println("Davai po novoi)))))")
                println("[any number] - select theme [back] - Back")
            } else {
                val messages = forumService.getAllMessageByTheme(packetMessageDTO)
                if (messages != null) {
                    for (message in messages) println("<${message.dateTime}>[${message.userName}] ${message.message}")
                }
                if (messages == null) println("I haven't message, but you can become the first")
                menuLevelThird(forumService, scanner, packetMessageDTO)
            }
        }
    }

    private fun menuLevelThird(forumService: ForumService, scanner: Scanner, packetMessageDTO: PacketMessageDTO) {
        println("Write message or update screen")
        println("[update] - update [back] - back")
        conditionLevelThird = true
        while (conditionLevelThird) actionLevelThird(forumService, scanner, packetMessageDTO)
    }

    private fun actionLevelThird(forumService: ForumService, scanner: Scanner, packetMessageDTO: PacketMessageDTO) {
        when (val input = scanner.nextLine()) {
            "back" -> {
                conditionLevelThird = false
                conditionLevelSecond = false
                println("[1] - Check forum tree [2] - Check active users [3] - Exit")
            }
            "update" -> {
                val messageList = forumService.getNewMessageByTheme(packetMessageDTO)
                if (messageList != null) {
                    for (message in messageList) {
                        println("<${message.dateTime}>[${message.userName}] ${message.message}")
                    }
                    println("[update] - update [back] - back")
                }
            }
            else -> {
                val messageModel = forumService.sendMessage(
                    MessageModelDTO(
                        forumService.userName,
                        input,
                        packetMessageDTO.mainTheme,
                        packetMessageDTO.subTheme
                    )
                )
                println("<${messageModel!!.dateTime}>[${messageModel.userName}] ${messageModel.message}")
            }
        }
    }

    private fun printForumTree(structureForumModel: StructureForumModel) {
        var count = 1
        for (mainTheme in structureForumModel.mainThemeList) {
            println("[$count] ${mainTheme.name}")
            var countSub = 1
            for (subThemes in mainTheme.subThemeList) {
                println("   |____[$count.$countSub] ${subThemes.name}")
                countSub++
            }
            count++
        }
    }

    private fun getPacketMessageOrNull(
        input: String,
        structureForumModel: StructureForumModel
    ): PacketMessageDTO? {
        val parts = input.trim().split(DOT)
        if (parts.size != 2) return null
        if (parts[0] == VOID || parts[1] == VOID) return null
        val newList = parts.map { it.toInt() - 1 }
        if (!checkInterval(structureForumModel.mainThemeList, newList[0])) return null
        val mainTheme = structureForumModel.mainThemeList[newList[0]]
        if (!checkInterval(mainTheme.subThemeList, newList[1])) return null
        return PacketMessageDTO(mainTheme.name, mainTheme.subThemeList[newList[1]].name)
    }

    private fun <T> checkInterval(list: List<T>, number: Int): Boolean {
        return number in list.indices
    }
}