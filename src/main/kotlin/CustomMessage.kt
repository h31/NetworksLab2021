data class CustomMessage constructor(var time: String = "", var name: String = "", var msg: String = "",
                                     var attname: String = "", var att: String = "") {
    override fun toString(): String {
        return "time: $time\nname: $name\nmsg: $msg\nattname: $attname\natt: $att\n"
    }
}