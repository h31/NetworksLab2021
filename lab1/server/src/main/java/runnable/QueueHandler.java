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


                for (Map.Entry<String, ServerStart.ClientHandler> activeUser : ServerStart.clientMap.entrySet()) {
                    ServerStart.ClientHandler value = activeUser.getValue();
                    value.out.println(headOfQueue.toParcel());
                    if (headOfQueue.getAttachmentSize() != 0) {
                        byte[] bytes = headOfQueue.getAttachmentByteArray();
                        value.dOut.write(bytes, 0, bytes.length);
                        System.out.println("сервер отправил клиенту " + headOfQueue.getUsername());
                    }
                }
                System.out.println("файл отправлен всем клиентам");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }


        }
    }
}
