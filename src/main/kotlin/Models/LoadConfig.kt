package Models

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.validate
import nicknameRegex

sealed class LoadConfig(name: String): OptionGroup(name) {
    class ClientType : LoadConfig("Options for loading client") {
        val nickname by option("-n", "--nickname", help = "User nickname for chat")
            .prompt(text = "Please, enter you're nickname")
            .validate {
                require(it.matches(nicknameRegex)) {
                    "Nickname can consist only of any combination of letters and digits."
                }
                require(it.toLowerCase() != "server") {
                    "Any case 'Server' nickname can not be taken. Choose another one."
                }
            }
    }

    class ServerType : LoadConfig("Options for loading server")
}

