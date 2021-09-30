import runnable.ServerStart;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        ServerStart serverStart = new ServerStart();

        try {
            serverStart.start(6666);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
