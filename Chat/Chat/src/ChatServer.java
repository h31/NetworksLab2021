import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.*;


class Session implements Runnable {
    private static int count = 0;
    private final Socket socket;
    private String nickname;
    private final InputStream is;
    private final OutputStream os;
    private final Scanner sc;
    private final PrintWriter pw;
    private final Coder coder = new Coder();
    private final Set<Session> sessions;

    public Session(Socket s, Set<Session> sessions) throws IOException {
        this.socket = s;
        this.is = s.getInputStream();
        this.os = s.getOutputStream();
        this.sc = new Scanner(is);
        this.pw = new PrintWriter(os);
        this.sessions = sessions;
        this.nickname = "guest" + ++count;
        pw.println("type=init&nickname=" + nickname);
        System.out.println("type=init&nickname=" + nickname);
        pw.flush();
    }

    public void run() {
        try {
            while (sc.hasNext()) {
                String msg = sc.nextLine();
                if (msg.equals("exit")) {
                    break;
                }
                handleMessage(msg);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        finally {
            close();
        }
    }

    public void close() {
        try {
            socket.close();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        finally {
            sessions.remove(this);
        }
    }

    public void shutdown() {
        pw.println("type=shutdown");
        pw.flush();
        close();
    }

    private void handleMessage(String msg) throws IOException {
        Map<String, String> input = coder.decodeData(msg);

        String type = input.get("type");

        switch (type) {
            case "nickname":
                nickname = input.get("nickname");
                break;
            case "text": {
                Map<String, String> data = new HashMap<>();
                data.put("type", "text");
                data.put("timestamp", String.valueOf(getUTCTimestamp()));
                data.put("nickname", nickname);
                data.put("text", input.get("text"));
                String output = coder.encodeData(data);
                for (Session s : sessions) {
                    if (s != this) {
                        s.pw.println(output);
                        s.pw.flush();
                    }
                }
                break;
            }
            case "file": {
                Map<String, String> data = new HashMap<>();
                data.put("type", "file");
                data.put("timestamp", String.valueOf(getUTCTimestamp()));
                data.put("nickname", nickname);
                data.put("filename", input.get("filename"));
                data.put("filesize", input.get("filesize"));
                String output = coder.encodeData(data);
                System.out.println("reading bytes...");

                byte[] bytes = new byte [Integer.parseInt(input.get("filesize"))];
                int bytesread = is.read(bytes, 0, bytes.length);
                System.out.println("bytes received: " + Arrays.toString(bytes));
                for (Session s : sessions) {
                    if (s != this) {
                        s.pw.println(output);
                        s.pw.flush();
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        s.os.write(bytes);
                        System.out.println("file sent");
                    }
                }
                break;
            }
            case "error":
                System.out.println("Error: " + input.get("message"));
                break;
        }
    }

    static long getUTCTimestamp() {
        return Instant.now().toEpochMilli() / 1000L;
    }

}

public class ChatServer {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(8777);
        System.out.println("Server started");
        Set<Session> sessions = new HashSet<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().interrupt();
            try {
                for (Session s : new ArrayList<>(sessions)) {
                    s.shutdown();
                }
                ss.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                System.out.println("Server stopped");
            }
        }));
        while (true) {
            Socket s;
            try {
                s = ss.accept();
            }
            catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            Session session;
            try {
                session = new Session(s, sessions);
            } catch (IOException e) {
                e.printStackTrace();
                s.close();
                continue;
            }
            sessions.add(session);
            Thread t = new Thread(session);
            t.start();
        }
    }
}
