import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ChatClient {
    static byte[] readBytes(InputStream stream, int dataSize) throws IOException {
        int totalBytesRead = 0;
        byte[] data = new byte[dataSize];

        while (totalBytesRead < dataSize) {
            int bytesRemaining = dataSize - totalBytesRead;
            int bytesRead = stream.read(data, totalBytesRead, bytesRemaining);

            if (bytesRead == -1) {
                throw new IOException("Socket is closed");
            }
            totalBytesRead += bytesRead;
        }
        return data;
    }

    public static void main(String[] args) throws Exception {
        Socket s = new Socket(InetAddress.getLocalHost(), 8777);
        InputStream is = s.getInputStream();
        Scanner sc = new Scanner(s.getInputStream());
        OutputStream os = s.getOutputStream();
        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        Coder coder = new Coder();

        Thread t = new Thread(() -> {
            while (true) {
                if (!sc.hasNext()) {
                    return;
                }
                String sInp = sc.nextLine();
                Map<String, String> data = coder.decodeData(sInp);
                String type = data.get("type");
                switch (type) {
                    case "text" -> {
                        Timestamp timestamp = new Timestamp(Long.parseLong(data.get("timestamp")));
                        LocalDateTime ldt = timestamp.toLocalDateTime();
                        System.out.printf(
                                "<%02d:%02d> [%s] > %s\n",
                                ldt.getHour(),
                                ldt.getMinute(),
                                data.get("nickname"),
                                data.get("text")
                        );
                    }
                    case "file" -> {
                        Timestamp timestamp = new Timestamp(Long.parseLong(data.get("timestamp")));
                        LocalDateTime ldt = timestamp.toLocalDateTime();
                        System.out.printf(
                                "<%02d:%02d> [%s] @ attached a file: %s\n",
                                ldt.getHour(),
                                ldt.getMinute(),
                                data.get("nickname"),
                                data.get("filename")
                        );
                        try {
                            byte[] bytes = readBytes(is, Integer.parseInt(data.get("filesize")));
                            String path = "files/" + data.get("filename");
                            FileOutputStream fos = new FileOutputStream(path);
                            fos.write(bytes);
                            fos.close();
                            System.out.println("File is saved in " + path);
                        }
                        catch (Exception e) {
                            System.err.println("Error while reading file");
                        }
                    }
                    case "shutdown" -> {
                        System.out.println("=".repeat(40));
                        System.out.println("Server is shutdown");
                        System.out.println("=".repeat(40));
                        System.exit(1);
                    }
                    case "error" -> System.out.println("Error: " + data.get("message"));
                }
            }
        });
        t.start();


        Scanner scConsole = new Scanner(System.in);

        {
            System.out.println("Nickname:");
            String nickname = scConsole.nextLine();
            Map<String, String> data = new HashMap<>();
            data.put("type", "nickname");
            data.put("nickname", nickname);
            pw.println(coder.encodeData(data));
        }

        while (true) {
            System.out.print("> ");
            String input = scConsole.nextLine();
            if (input.isEmpty()) {
                continue;
            }

            if (input.equals("exit")) {
                pw.println(input);
                break;
            }
            else if (input.startsWith("@file")) {
                File file = new File(input.substring(6));
                if (!file.isFile()) {
                    System.out.println("The file does not exist");
                    continue;
                }
                FileInputStream fis = new FileInputStream(file);
                byte[] bytes = fis.readAllBytes();
                fis.close();

                Map<String, String> data = new HashMap<>();
                data.put("type", "file");
                data.put("filename", file.getName());
                data.put("filesize", String.valueOf(bytes.length));
                pw.println(coder.encodeData(data));
                Thread.sleep(1);
                os.write(bytes);
                System.out.println("File is sent");
            }
            else {
                Map<String, String> data = new HashMap<>();
                data.put("type", "text");
                data.put("text", input);
                pw.println(coder.encodeData(data));
            }
        }

        sc.close();
        pw.close();
        s.close();
    }
}
