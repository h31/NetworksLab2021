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

    public static ExchangeFormat parseRequest(String format) {
        System.out.println("че пришло: " + format);
        String[] array = Arrays.asList(format.split("[({')|(':')|(', ')|('})]"))
                .stream().filter(str -> !str.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);

        ExchangeFormat serverRequest = new ExchangeFormat();
        serverRequest.setTime(new Date().toString());
        serverRequest.setMessage(array[3]);
        serverRequest.setParcelType(array[1]);

        return serverRequest;
    }
}
