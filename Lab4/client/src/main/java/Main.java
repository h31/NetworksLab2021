import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static RetrofitController controller = new RetrofitController();
    private static Scanner scanner = new Scanner(System.in);
    private static String currentUsername;

    public static void main(String[] args) {
        int userChoice = authChoice();

        try {
            switch (userChoice) {
                case 1:
                    registration();
                    System.out.println("Now log in....");
                    login();
                    break;
                case 2:
                    login();
                    break;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public static int authChoice() {
        int result;

        System.out.println("If you already have an account - login, if not - register");
        System.out.println("-------------------------\n");
        System.out.println("1 - Registration");
        System.out.println("2 - Login");

        result = scanner.nextInt();
        return result;
    }

    private static void registration() throws IOException {
        boolean isRegistrationSuccessful = false;
        while (!isRegistrationSuccessful) {
            System.out.println("Enter your login");
            String username = scanner.next();
            System.out.println("Enter your password");
            String password = scanner.next();
            isRegistrationSuccessful = controller.registration(username, password);
        }
    }

    private static void login() throws IOException {
        boolean isLoginSuccessful = false;
        String username = "";
        while (!isLoginSuccessful) {
            System.out.println("Enter your login");
            username = scanner.next();
            System.out.println("Enter your password");
            String password = scanner.next();
            isLoginSuccessful = controller.authentication(username, password);
        }
        currentUsername = username;
        System.out.println("Hello, " + currentUsername);
    }
}
