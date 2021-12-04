import com.poly.client.MessageData
import com.poly.client.MessageData.userName
import com.poly.client.util.USER_HOME
import org.junit.Assert
import org.junit.Test
import java.io.File

class MessageDataTest {

    @Test
    fun testBadFilePath() {
        userName = "nikita"
        val message1 = MessageData.createMessage("privet, kak dela fp:-/ ")
        val message2 = MessageData.createMessage("privet, kak dela fp:-/ ${System.getProperty(USER_HOME)}")
        Assert.assertEquals(
            "Message{date='null', name='nikita', text='privet, kak dela fp:-/ ', fileName='null'}",
            message1.message.toString()
        )
        Assert.assertEquals(
            "Message{date='null', name='nikita', text='privet, kak dela fp:-/ " +
                    "${System.getProperty(USER_HOME)}', fileName='null'}",
            message2.message.toString()
        )
    }

    @Test
    fun testWellFilePath() {
        userName = "nikita"
        val file = File("${System.getProperty(USER_HOME)}${File.separator}test.txt")
        file.createNewFile()
        val message = MessageData.createMessage("privet, kak dela fp:-/ ${file.absolutePath}")
        Assert.assertEquals(
            "Message{date='null', name='nikita', text='privet, kak dela ', fileName='${file.name}'}",
            message.message.toString()
        )
        file.delete()
    }

    @Test
    fun testRussianLanguage() {
        userName = "Никита"
        val message = MessageData.createMessage("Привет, как дела: что делаешь?")
        Assert.assertEquals(
            "Message{date='null', name='Никита', text='Привет, как дела: что делаешь?', fileName='null'}",
            message.message.toString()
        )
    }
}