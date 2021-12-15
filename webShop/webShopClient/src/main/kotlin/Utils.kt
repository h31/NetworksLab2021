const val ip = "26.82.19.20"
const val port = "5555"
const val baseUrl = "http://$ip:$port"

fun printHelpMsg() {
    println("Now you can do several things:")
    println("list - lists all the positions")
    println("addPos [name] [amount] [price] - add BRAND NEW position of items with certain initial amount and certain price")
    println("buy [name] [amount] - buy certain amount of product from the shop (amount of goods changes, of course)")
    println("add [name] [amount] - add certain amount of product to the shop (amount of goods changes, of course)")
    println("exit - you know, obviously.")
    println("help - look at this message again. Beautiful, isn't it?")
}