import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class Util {

    public enum Request {
        GREETING("greeting"),
        EXCEPTION("exception"),
        EXIT("exit"),
        MESSAGE("message"),
        INFO("info");

        private String stringValue;

        Request(String stringValue) {
            this.stringValue = stringValue;
        }

        public String getStringValue() {
            return stringValue;
        }
    }

    // Имя файлов в винде не должно содержать: \ / : * ? " < > |
    public static ExchangeFormat parseRequest(String format) {
        System.out.println(format);
        String[] array = format.split("(?<!\\\\)'");

        System.out.println(Arrays.toString(array));

        String[] removedUnnecessary = Arrays.stream(array)
                .filter(value ->
                        !value.equals(", ") && !value.equals(":")
                )
                .toArray(String[]::new);


        // поменять парсер, потому что regex не понимает 'username':'//'
        // он думает, что эскейпится последняя ковычка, хотя по факту эскейп символ / был уже эскейпнут до этого


        ExchangeFormat exchangeFormat = new ExchangeFormat();
        exchangeFormat.setParcelType(removedUnnecessary[2]);
        exchangeFormat.setMessage(removedUnnecessary[4]);
        exchangeFormat.setUsername(removedUnnecessary[6]);
        exchangeFormat.setAttachmentType(removedUnnecessary[8]);


        return exchangeFormat;
    }

}
