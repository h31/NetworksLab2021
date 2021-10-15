package runnable;

import model.ExchangeFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class QueueHandler implements Runnable {

    BlockingQueue<ExchangeFormat> serverResponseQueue;


    public QueueHandler(BlockingQueue<ExchangeFormat> serverResponseQueue) {
        this.serverResponseQueue = serverResponseQueue;
    }

    @Override
    public void run() {
        ExchangeFormat headOfQueue;
        while (true) {
            try {
                headOfQueue = serverResponseQueue.take();
                String messageAuthor = headOfQueue.getUsername();
                int fileSize = headOfQueue.getAttachmentSize();
                if (fileSize != 0) {
                    ClientHandler addresseeSocket;
                    String addresseeUsername;
                    for (Map.Entry<String, ClientHandler> activeUser : ServerStart.clientMap.entrySet()) {
                        addresseeSocket = activeUser.getValue();
                        addresseeUsername = activeUser.getKey();
                        // отослать автору его же сообщение обратно
                        if (addresseeUsername.equals(messageAuthor)) {
                            headOfQueue.setAttachmentSize(0);
                            addresseeSocket.out.println(headOfQueue.toParcel());
                        }
                        // бродкаст сообщение + файл всем кроме автора
                        if (!addresseeUsername.equals(messageAuthor)) {
                            System.out.println("File will be sent to the " + addresseeUsername);
                            byte[] bytes = headOfQueue.getAttachmentByteArray();
                            headOfQueue.setAttachmentSize(fileSize);
                            addresseeSocket.out.println(headOfQueue.toParcel());
                            addresseeSocket.dOut.size()  write(bytes, 0, bytes.length);
                            System.out.println("File was sent to the  " + addresseeUsername);
                        }
                    }
                    System.out.println("File sent to all clients");
                } else {
                    for (Map.Entry<String, ClientHandler> activeUser : ServerStart.clientMap.entrySet()) {
                        ClientHandler value = activeUser.getValue();
                        value.out.println(headOfQueue.toParcel());
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }


        }
    }
}
