package Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MessageCreator {

    public static byte[] createMessage(String userName, char command) {
        return createMessage(userName, command, "", "", 0, null, "");
    }

    public static byte[] createMessage(String userName, char command, String text, String date) {
        return createMessage(userName, command, date, "", 0, null, text);
    }

    public static byte[] createMessage(String userName,
                                       char command,
                                       String date,
                                       String fileName,
                                       int fileLength,
                                       byte[] fileBytes,
                                       String text) {
        String newDate = "";
        if (!date.isEmpty()) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(date));
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            newDate = ldt.toString().replace("T", " ");

            if (command == Command.GREETING.getSymbol()) {
                System.out.println(userName + " joined the server at " + newDate);

            }
            if (command == Command.TEXT.getSymbol()) {
                if (fileLength == 0) {
                    System.out.println("<" + newDate + "> [" + userName + "] ");
                } else {
                    System.out.println("<" + newDate + "> [" + userName + "] " +
                            ", file: " + fileName);
                }
            }
            if (command == Command.CLOSE.getSymbol()) {
                System.out.println(userName + " left the server at " + newDate);
            }
        }

        String messageWithoutSize =
                "','u':'" + userName.replace("'", "'\\") +
                        "','c':'" + command +
                        "','t':'" + text +
                        "','d':'" + date +
                        "','f':'" + fileName +
                        "','l':'" + fileLength +
                        "','b':'";
        byte[] bytes;
        if (fileLength == 0) {
            messageWithoutSize += "']";
            String length = String.valueOf(messageWithoutSize.length());
            length = addZeros(length);
            String message = "['s':'" + length + messageWithoutSize;
            bytes = message.getBytes();
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(messageWithoutSize.getBytes());
                outputStream.write(fileBytes);
                outputStream.write("']".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String length = String.valueOf(outputStream.size());
            length = addZeros(length);
            String message = "['s':'" + length;

            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
            try {
                outputStream2.write(message.getBytes());
                outputStream2.write(messageWithoutSize.getBytes());
                outputStream2.write(fileBytes);
                outputStream2.write("']".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            bytes = outputStream2.toByteArray();
        }
        return bytes;
    }

    private static String addZeros(String length) {
        switch (length.length()) {
            case 1:
                length = "00000" + length;
                break;
            case 2:
                length = "0000" + length;
                break;
            case 3:
                length = "000" + length;
                break;
            case 4:
                length = "00" + length;
                break;
            case 5:
                length = "0" + length;
                break;
        }
        return length;
    }

}
