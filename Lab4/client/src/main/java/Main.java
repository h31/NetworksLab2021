import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int authUserChoice = ConsoleMenuAdapter.authChoice();
        try {
            switch (authUserChoice) {
                case 1:
                    ConsoleMenuAdapter.registration();
                    System.out.println("Now log in....");
                    ConsoleMenuAdapter.login();
                    break;
                case 2:
                    ConsoleMenuAdapter.login();
                    break;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        while (true) {
            int operationUserChoice = ConsoleMenuAdapter.opChoice();
            switch (operationUserChoice) {
                case 1:
                    sum();
                    break;
                case 2:
                    sub();
                    break;
                case 3:
                    mul();
                    break;
                case 4:
                    div();
                    break;
                case 5:
                    sqrt();
                    break;
                case 6:
                    fact();
                    break;
                case 7:
                    return;
            }
        }
    }


    private static void sum() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            ConsoleMenuAdapter.controller.getSum(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void sub() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            ConsoleMenuAdapter.controller.getSub(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void mul() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            ConsoleMenuAdapter.controller.getMul(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void div() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            ConsoleMenuAdapter.controller.getDiv(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void sqrt() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            ConsoleMenuAdapter.controller.getSqrt(list.toString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void fact() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            ConsoleMenuAdapter.controller.getFact(list.toString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

}
