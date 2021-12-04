import com.poly.models.Message;
import com.poly.models.MessageWithContent;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolySerializeTest {

    @Test
    public void serializeTest() {
        Message message = new Message(
                "2021-09-23", "Nikita", "Привет! Как дела? Я делаю домашнее задание на тему: компьютерные сети, а ты?", null, null);
        Message messageAfterParse = new Message();
        messageAfterParse.parseToMessage(message.toTransferString());
        Assertions.assertEquals(message, messageAfterParse);
    }

    @Test
    public void serializeTest2() {
        Message message = new Message(
                "2021-09-23", "Nikita", "Татьяна, состоявшая, как мы сказали выше, в должности прачки (впрочем, ей, как искусной и ученой прачке, поручалось одно тонкое белье), была женщина лет двадцати осьми, маленькая, худая, белокурая, с родинками на левой щеке. Родинки на левой щеке почитаются на Руси худой приметой -- предвещанием несчастной жизни... Татьяна не могла похвалиться своей участью. С ранней молодости ее держали в черном теле; работала она за двоих, а ласки никакой никогда не видала; одевали ее плохо, жалованье она получала самое маленькое; родни у ней все равно что не было: о  ", null, null);
        Message messageAfterParse = new Message();
        messageAfterParse.parseToMessage(message.toTransferString());
        System.out.println(message);
        System.out.println(message.toTransferString());
        System.out.println(messageAfterParse);
        Assertions.assertEquals(message, messageAfterParse);
    }

    @Test
    public void sizeConverterTest() {
        MessageWithContent some = new MessageWithContent(new Message(), new byte[4]);
        byte[] size = some.getSizeMessage(209);
        for (byte b : size) {
            System.out.println(b);
        }
    }

    private byte[] readByteMessage(byte[] bytes) {
        int size = 0;
        for (int i = 0; i < 4; i++) {
            size = size << 8;
            size += bytes[i] & 0xff;
        }
        System.out.println(size);
        System.out.println(bytes.length);
        System.out.println(size + 4);
        byte[] msg = new byte[size];
        for (int i = 4; i < size + 4; i++) {
            msg[i - 4] = bytes[i];
        }
        return msg;
    }

    @Test
    public void readMessageTest() {
        byte[] bytes = new byte[]{0, 0, 0, -47, 100, 97, 116, 101, 32, 58, 32, 50, 48, 50, 49, 45, 49, 50, 45, 48, 52, 32, 48, 51, 46, 51, 54, 46, 53, 51, 46, 55, 55, 57, 32, 44, 32, 110, 97, 109, 101, 32, 58, 32, 110, 105, 107, 105, 116, 97, 32, 44, 32, 116, 101, 120, 116, 32, 58, 32, 98, 104, 106, 107, 101, 108, 119, 100, 102, 109, 101, 118, 106, 110, 32, 103, 101, 106, 107, 102, 110, 101, 114, 106, 107, 32, 110, 102, 114, 106, 101, 107, 32, 101, 110, 102, 106, 101, 110, 114, 102, 106, 32, 101, 110, 107, 106, 101, 102, 110, 101, 114, 106, 107, 102, 32, 110, 101, 106, 107, 114, 110, 102, 101, 106, 107, 114, 110, 32, 102, 101, 106, 107, 114, 110, 102, 32, 101, 106, 114, 107, 32, 102, 110, 101, 114, 106, 102, 110, 32, 101, 114, 106, 107, 102, 32, 110, 101, 114, 106, 102, 101, 114, 106, 107, 102, 32, 110, 101, 114, 107, 106, 102, 110, 106, 101, 32, 32, 44, 32, 102, 105, 108, 101, 78, 97, 109, 101, 32, 58, 32, 110, 117, 108, 108, 32, 44, 32, 102, 105, 108, 101, 83, 105, 122, 101, 32, 58, 32, 110, 117, 108, 108};
        byte[] bytes2 = new byte[]{100, 97, 116, 101, 32, 58, 32, 50, 48, 50, 49, 45, 49, 50, 45, 48, 52, 32, 48, 51, 46, 51, 54, 46, 53, 51, 46, 55, 55, 57, 32, 44, 32, 110, 97, 109, 101, 32, 58, 32, 110, 105, 107, 105, 116, 97, 32, 44, 32, 116, 101, 120, 116, 32, 58, 32, 98, 104, 106, 107, 101, 108, 119, 100, 102, 109, 101, 118, 106, 110, 32, 103, 101, 106, 107, 102, 110, 101, 114, 106, 107, 32, 110, 102, 114, 106, 101, 107, 32, 101, 110, 102, 106, 101, 110, 114, 102, 106, 32, 101, 110, 107, 106, 101, 102, 110, 101, 114, 106, 107, 102, 32, 110, 101, 106, 107, 114, 110, 102, 101, 106, 107, 114, 110, 32, 102, 101, 106, 107, 114, 110, 102, 32, 101, 106, 114, 107, 32, 102, 110, 101, 114, 106, 102, 110, 32, 101, 114, 106, 107, 102, 32, 110, 101, 114, 106, 102, 101, 114, 106, 107, 102, 32, 110, 101, 114, 107, 106, 102, 110, 106, 101, 32, 32, 44, 32, 102, 105, 108, 101, 78, 97, 109, 101, 32, 58, 32, 110, 117, 108, 108, 32, 44, 32, 102, 105, 108, 101, 83, 105, 122, 101, 32, 58, 32, 110, 117, 108, 108};
        byte[] msg = readByteMessage(bytes);
        Message message1 = new Message();
        Message message2 = new Message();
        message1.parseToMessage(StringUtils.newStringUtf8(bytes2));
        message2.parseToMessage(StringUtils.newStringUtf8(msg));
        Assertions.assertEquals(message1, message2);
    }
}
