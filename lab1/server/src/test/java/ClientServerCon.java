import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientServerCon {

    @Test
    public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() {
        Client client = new Client();
        try {
            client.startConnection(null, 6666);
            assertEquals("hello client", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
