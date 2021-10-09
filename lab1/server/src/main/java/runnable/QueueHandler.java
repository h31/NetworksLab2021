package runnable;

import model.ExchangeFormat;

import java.io.IOException;
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
                if (headOfQueue.getAttachmentSize() != 0) {
                    for (Map.Entry<String, ClientHandler> activeUser : ServerStart.clientMap.entrySet()) {
                        if (activeUser.getKey().equals(messageAuthor)) {

                        }
                        ClientHandler value = activeUser.getValue();
                        value.out.println(headOfQueue.toParcel());

                        if (!activeUser.getKey().equals(messageAuthor)) {
                            System.out.println("File will be sent to the " + activeUser.getKey());
                            byte[] bytes = headOfQueue.getAttachmentByteArray();
                            value.dOut.write(bytes, 0, bytes.length);
                            System.out.println("File was sent to the  " + activeUser.getKey());
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
