package Utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Parser {
    private final String userName;
    private final char command;
    private final String text;
    private final String date;
    private final String fileName;
    private final int fileLength;
    private final byte[] fileBytes;

    public Parser(String userName,
                  char command,
                  String text,
                  String date,
                  String fileName,
                  int fileLength,
                  byte[] fileBytes) {
        this.userName = userName;
        this.command = command;
        this.text = text;
        this.date = date;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.fileBytes = fileBytes;
    }

    public String getUserName() {
        return userName;
    }

    public char getCommand() {
        return command;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileLength() {
        return fileLength;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public static String createField(byte[] bytes, int index) {
        String field = "";
        int ind = index + 5;
        StringBuilder fieldBuilder = new StringBuilder(field);
        while (true) {
            if (bytes[ind] != '\'') {
                fieldBuilder.append((char) bytes[ind]);
                ind++;
            } else {
                if (bytes[ind + 1] == '\\') {
                    fieldBuilder.append((char) bytes[ind]);
                    ind += 2;
                } else {
                    break;
                }
            }
        }

        field = fieldBuilder.toString();
        return field;
    }

    public static String getField(byte[] bytes, char key) {
        int index = 0;
        while (true) {
            index++;
            if (bytes[index] == '\'' && bytes[index + 1] == key &&
                    bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                if (key=='d') {
                    String dateInString = createField(bytes, index);
                    if (!dateInString.isEmpty()) {
                        Instant instant = Instant.ofEpochMilli(Long.parseLong(dateInString));
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                        return ldt.toString().replace("T", " ");
                    }
                    return "";
                }
                return createField(bytes, index);
            }
        }
    }

    public static Parser getAll(byte[] bytes) {
        int index = 0;
        String userName;
        char command;
        String date;
        String fileName;
        String text;
        int fileLength;
        userName = getField(bytes, 'u');
        command = getField(bytes, 'c').charAt(0);
        text = getField(bytes, 't');
        date = getField(bytes, 'd');
        fileName = getField(bytes, 'f');
        fileLength = Integer.parseInt(getField(bytes, 'l'));

        byte[] fileBytes = new byte[fileLength];
        while (true) {
            index++;
            try {
                if (bytes[index] == '\'' && bytes[index + 1] == 'b' &&
                        bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                    for (int i = index + 5; i < index + 5 + fileLength; i++) {
                        fileBytes[i - index - 5] = bytes[i];
                    }
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return new Parser(userName, command, text, date, fileName, fileLength, fileBytes);

    }
}
