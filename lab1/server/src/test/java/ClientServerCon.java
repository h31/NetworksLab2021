import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
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
        String format1 = "{'parcelType':'greeting', 'message':'sak\\'fhas\\'jklas', 'username':'\', 'attachment':'.jpg', 'file':''}";
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


}
