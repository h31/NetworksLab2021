import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tool {

    public enum RequestType {
        GREETING("greeting"),
        EXCEPTION("exception"),
        EXIT("exit"),
        MESSAGE("message"),
        INFO("info");

        private String stringValue;

        RequestType(String stringValue) {
            this.stringValue = stringValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public static RequestType findByValue(String s) {
            for (RequestType e : RequestType.values()) {
                if (s.equals(e.getStringValue())) return e;
            }
            return null;
        }
    }


    public static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(new Date());
    }


    public static ExchangeFormat parseRequest(String format) {


        List<String> resultArray = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < format.length(); i++) {
            if (format.charAt(i) == '\'') {
                for (int j = i + 1; j < format.length(); j++) {
                    if (format.charAt(j) == '\\' && format.charAt(j + 1) == '\\') {
                        stringBuilder.append(format.charAt(j)).append(format.charAt(j + 1));
                        j = j + 1;
                        continue;
                    }
                    if (format.charAt(j) == '\\' && format.charAt(j + 1) == '\'') {
                        stringBuilder.append(format.charAt(j)).append(format.charAt(j + 1));
                        j = j + 1;
                        continue;
                    }
                    if (format.charAt(j) == '\'') {
                        resultArray.add(stringBuilder.toString());
                        stringBuilder.setLength(0);
                        i = j + 1;
                        break;
                    }
                    stringBuilder.append(format.charAt(j));
                }
            }
        }
        ExchangeFormat exchangeFormat = new ExchangeFormat();
        try {
            exchangeFormat.setParcelType(RequestType.findByValue(resultArray.get(1)));
            exchangeFormat.setMessage(resultArray.get(3));
            exchangeFormat.setUsername(resultArray.get(5));
            exchangeFormat.setAttachmentName(resultArray.get(7));
            exchangeFormat.setAttachmentSize(Integer.parseInt(resultArray.get(9)));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            ExchangeFormat exceptionResponse = new ExchangeFormat();
            exceptionResponse.setParcelType(RequestType.EXCEPTION);
            exceptionResponse.setMessage("Invalid parcel format");
            return exceptionResponse;
        }

        return exchangeFormat;
    }

}