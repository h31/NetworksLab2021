import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static String calcFast(String op, Scanner sc, ApiClient apiClient) {
        System.out.println("Введите a:");
        double a = sc.nextDouble();
        System.out.println("Введите b:");
        double b = sc.nextDouble();
        return apiClient.get(op + "?a=" + a + "&b" + b) ;
    }

    private static void calcSlow(String op, Scanner sc, WebSocketClient webSocketClient) {
        System.out.println("Введите аргумент:");
        double a = sc.nextDouble();
        webSocketClient.sendMessage(op + " " + a);
    }

    private static String authorize(ApiClient apiClient, String login, String password) {
        String result = apiClient.post("login", "login=" + login + "&password=" + password);
        JsonObject resp = JsonParser.parseString(result).getAsJsonObject();
        if (resp.get("status").getAsString().equals("ok")) {
            return resp.get("sid").getAsString();
        }
        throw new RuntimeException("Access denied");
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Args: server port login password");
            return;
        }
        String server = args[0];
        String port = args[1];
        String login = args[2];
        String password = args[3];

        ApiClient apiClient = new ApiClient("http://" + server + ":" + port + "/");

        String sid;
        try {
            sid = authorize(apiClient, login, password);
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }

        WebSocketClient webSocketClient = new WebSocketClient("ws://" + server + ":" + port);

        webSocketClient.addMessageHandler((message) -> {
            String messageType = message.get("type").getAsString();
            switch (messageType) {
                case "answer": {
                    System.out.printf(
                            "%s(%s) = %s\n",
                            message.get("func").getAsString(),
                            message.get("arg").getAsInt(),
                            message.get("result").getAsInt()
                    );
                    break;
                }
                case "disconnect": {
                    try {
                        webSocketClient.userSession.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Соединение с сервером разорвано");
                    System.exit(1);
                }
            }
        });
        webSocketClient.sendMessage("sid " + sid);

        Scanner sc = new Scanner(System.in);
        w1: while (true) {
            System.out.println("1. Вычислить результат операции");
            System.out.println("2. Выход");
            String action = sc.next();
            s1: switch (action) {
                case "1": {
                    System.out.println("1. Сумма");
                    System.out.println("2. Разность");
                    System.out.println("3. Произведение");
                    System.out.println("4. Частное");
                    System.out.println("5. Факториал");
                    System.out.println("6. Квадратный корень");
                    System.out.println("7. Назад");

                    String[] options = {"sum", "sub", "mul", "div", "fact", "sqrt"};

                    action = sc.next();

                    switch (action) {
                        case "1":
                        case "2":
                        case "3":
                        case "4":
                            System.out.println("Ответ сервера: " + calcFast(options[Integer.parseInt(action) - 1], sc, apiClient));
                            break;
                        case "5":
                        case "6":
                            calcSlow(options[Integer.parseInt(action) - 1], sc, webSocketClient);
                            System.out.println("Ответ скоро будет получен");
                            break;
                        case "7": break s1;
                    }
                    break;
                }
                case "2": {
                    break w1;
                }
            }
        }
        webSocketClient.sendMessage("disconnect");
    }
}


// Операции:
// sum, sub, mul, div, fact, sqrt

// POST http://abc.de/login
// login={login}&password={password}
// resp: {
//   "status": "ok", // or "error"
//   "sid": "zxcqwdsfdsadas" // generated session id
// }

// GET http://abc.de/sum?a=2&b=3
// 5

// websocket
// > "sid ssiidd"
// > "fact 4.0"
// < {"type":"answer","func":"fact","arg":"4.0","result":"24"}
// > "sqrt 4.0"
// < {"type":"answer","func":"fact","arg":"4.0","result":"2.0"}
// < {"type":"disconnect"}
// > "disconnect"
