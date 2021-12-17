import controller.RetrofitController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final RetrofitController controller = new RetrofitController();


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
                /*case 5:
                    sqrt();
                    break;
                case 6:
                    fact();
                    break;
                case 7:
                    return;
            }*/
            }
        }
    }




    private static void sum() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            controller.getSum(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void sub() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            controller.getSub(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void mul() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            controller.getMul(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }

    private static void div() {
        List<Double> list = ConsoleMenuAdapter.createQueryList();
        try {
            controller.getDiv(list.toString());
        } catch (IOException e) {
            System.out.println("Something goes wrong, try again");
        }
    }



}
