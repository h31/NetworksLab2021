import com.forum.client.model.PacketMessageDTO
import com.forum.client.service.ForumService
import org.junit.jupiter.api.Test

class ForumServiceTest {

    @Test
    fun getAllThemesTest() {
        val forumService = ForumService("user1", "password")
        println(forumService.getAllThemes().mainThemeList)
    }

    @Test
    fun getAllMessageByThemeTest() {
        val forumService = ForumService("user1", "password")
        println(forumService.getAllMessageByTheme(PacketMessageDTO("glurhh", "zrslvpfwianubawpbfrxzogtpcyni")))
    }


}