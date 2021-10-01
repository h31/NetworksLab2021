import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientServerCon {


    @Test
    public void testParseResponse() {
        String format = "{'responseType':'greeting'," +
                "'message':'eto soobsheniye'," +
                "'username':''," +
                "'attachment':''";
        String format1 = "{'responseType':'greeting','message':'[[[[',}";
        String[] result = format.split("[{}',]");
        String[] array = Arrays.asList(format.split("[({')|(':')|(',')|('})]"))
                .stream().filter(str -> !str.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);
        String[] result1 = format.split("[({')|(':')|(',')|('})]");
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(array));
        System.out.println(result[0].length());
    }


    // служебные слова: parcelType, message, username, attach
    @Test
    public void newParser() {
        String format = "{parcelType:greeting, message:eto : : :message: username;, , ,  ,soobsheniye, username:, attach:jaskfj}";
        String format1 = "{'parcelType':'greeting', 'message':'sak\\'fhas\\'jklas', 'username':'\\', 'attachment':'.jpg', 'file':''}";
        String formatWithoutBraces = format.substring(1, format.length() - 1);
        //System.out.println(formatWithoutBraces);


        String[] array = format1.split("(?<!\\\\)'");
        String[] arrayWithEmpty = format1.split("((?<!\\\\)':)|((?<!\\\\)')");

        System.out.println(Arrays.toString(array));

        String[] removedUnnecessary = Arrays.stream(array)
                .filter(value ->
                        !value.equals(", ") && !value.equals(":")
                )
                .toArray(String[]::new);
        System.out.println(Arrays.toString(removedUnnecessary));
    }


    List<String> resultArray = new ArrayList<>();
    StringBuilder stringBuilder = new StringBuilder();


    @Test
    public void manualParse() {
        String format = "{'parcelType':'greeting', 'message':'sak\\\\'fhas\\'jklas', 'username':'\\'э', 'attachment':'.jpg', 'file':''}";
        format = "{'username':'\\\\''}";
        format = "{'username':'\\'', 'attachmentType':''}\n";
        format = "{'parcelType':'greeting', 'message':'', 'username':'rock\\'n\\'roll', 'attachmentType':''}";
        format = "{'rock\\'n\\'roll'}";
        format = "{'\\\\\\'\\\\'\\\\\\'\\\\\\'\\\\\\'\\\\\\'\\\\\\'\\\\\\'\\\\\\'}";
        format = "{'\\\\\\''}";





        for (int i = 0; i < format.length(); i++) {
            if (format.charAt(i) == '\'') {
                for (int j = i + 1; j < format.length(); j++) {
                    if (format.charAt(j) == '\\' && format.charAt(j+1) == '\\') {
                            stringBuilder.append(format.charAt(j)).append(format.charAt(j+1));
                            j=j+1;
                            continue;
                        }
                    if (format.charAt(j) == '\\' && format.charAt(j+1) == '\'') {
                        stringBuilder.append(format.charAt(j)).append(format.charAt(j+1));
                        j = j+1;
                        continue;
                    }
                    if(format.charAt(j) == '\'') {
                        resultArray.add(stringBuilder.toString());
                        i = j+1;
                        break;
                    }
                    stringBuilder.append(format.charAt(j));
                    }
            }
        }

        System.out.println(resultArray);
    }


    // {'\\'} <> \
    // {'\\''} <> \'
    // {'\\\\''} <> \\'

}