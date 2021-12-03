package org.example.util;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    public static String readLine(InputStream in) throws IOException {
        char c;
        StringBuilder s = new StringBuilder();
        do {
            c = (char) in.read();
            if (c == '\n')
                break;
            s.append(c);
        } while (c != -1);
        return s.toString();
    }
}
