import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientServerCon {


    @Test
    public void testParseResponse() {
        String format = "{'responseType':'greeting', 'message':'[[[['}";
        String[] result = format.split("[{}',]");
        String[] array = Arrays.asList(format.split("[({')|(':')|(', ')|('})]"))
                .stream().filter(str -> !str.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);
        String[] result1 = format.split("[({')|(':')|(',')|('})]");
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(array));
        System.out.println(result[0].length());
    }

}
