package util;

import model.ExchangeFormat;

import java.util.ArrayList;
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
            for(RequestType e : RequestType.values()){
                if(s.equals(e.getStringValue())) return e;
            }
            return null;
        }
    }

    // Имя файлов в винде не должно содержать: \ / : * ? " < > |
    public static ExchangeFormat parseRequest(String format) {
        System.out.println(format);
        List<String> resultArray = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < format.length(); i++) {
            if (format.charAt(i) == '\'') {
                for (int j = i + 1; j < format.length(); j++) {
                    if (format.charAt(j) == '\\' && format.charAt(j + 1) == '\'') {
                        if (format.charAt(j + 2) == '\'') {
                            stringBuilder.append(format.charAt(j)).append(format.charAt(j + 1));
                            resultArray.add(stringBuilder.toString());
                            stringBuilder.setLength(0);
                            i = j + 2;
                            break;
                        }
                        stringBuilder.append(format.charAt(j));
                        continue;
                    }
                    if (format.charAt(j) == '\'') {
                        if (format.charAt(j + 1) == '\\') {
                            stringBuilder.append(format.charAt(j));
                        } else {
                            resultArray.add(stringBuilder.toString());
                            stringBuilder.setLength(0);
                            i = j + 1;
                            break;
                        }
                    } else {
                        stringBuilder.append(format.charAt(j));
                    }
                }
            }
        }

        System.out.println(resultArray);
        ExchangeFormat exchangeFormat = new ExchangeFormat();
        exchangeFormat.setParcelType(RequestType.findByValue(resultArray.get(1)));
        exchangeFormat.setMessage(resultArray.get(3));
        exchangeFormat.setUsername(resultArray.get(5));
        exchangeFormat.setAttachmentType(resultArray.get(7));
        // тимофей добавит
        //exchangeFormat.setAttachmentName(resultArray.get(9));
        //exchangeFormat.setAttachmentSize(resultArray.get(11));


        return exchangeFormat;
    }

}
