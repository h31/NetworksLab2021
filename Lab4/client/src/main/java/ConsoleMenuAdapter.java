import controller.RetrofitController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenuAdapter {

    private static final Scanner scanner = new Scanner(System.in);
    private static final RetrofitController controller = new RetrofitController();


    public static void registration() throws IOException {
        boolean isRegistrationSuccessful = false;
        while (!isRegistrationSuccessful) {
            System.out.println("Enter your login");
            String username = scanner.next();
            System.out.println("Enter your password");
            String password = scanner.next();
            isRegistrationSuccessful = controller.registration(username, password);
            if (!isRegistrationSuccessful) {
                System.out.println(" Something goes wrong, try again ");
            }
        }
    }

    public static void login() throws IOException {
        boolean isLoginSuccessful = false;
        String username = "";
        while (!isLoginSuccessful) {
            System.out.println("Enter your login");
            username = scanner.next();
            System.out.println("Enter your password");
            String password = scanner.next();
            isLoginSuccessful = controller.authentication(username, password);
            if (!isLoginSuccessful) {
                System.out.println("Something goes wrong, try again");
            }
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

    public static int opChoice() {
        int result;

        System.out.println("Choose an operation");
        System.out.println("-------------------------");
        System.out.println("1 - Sum of numbers");
        System.out.println("2 - Subtraction of numbers");
        System.out.println("3 - Multiplication of a set of numbers");
        System.out.println("4 - Sequential division of a set of numbers");
        System.out.println("5 - Square root for each number in the set");
        System.out.println("6 - Factorial for each number passed in the set");
        System.out.println("-------------------------");
        System.out.println("7 - Exit");

        result = scanner.nextInt();
        return result;
    }

    public static List<Double> createQueryList() {
        List<Double> list = new ArrayList<>();
        String inputString;
        double bufferDouble;
        System.out.println("Enter a number");
        while (!(inputString = scanner.next()).equals("quit")) {
            System.out.println("Enter a number");
            try {
                bufferDouble = Double.parseDouble(inputString);
            } catch (NumberFormatException e) {
                System.out.println("Enter only numbers");
                continue;
            }
            list.add(bufferDouble);
        }
        return list;
    }

}
