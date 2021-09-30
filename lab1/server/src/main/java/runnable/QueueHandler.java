package runnable;

import model.ExchangeFormat;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class QueueHandler implements Runnable{

    BlockingQueue<ExchangeFormat> serverResponseQueue;


    public QueueHandler(BlockingQueue<ExchangeFormat> serverResponseQueue) {
        this.serverResponseQueue = serverResponseQueue;
    }


    @Override
    public void run() {
        ExchangeFormat headOfQueue;
        while(true) {
            try {
                headOfQueue = serverResponseQueue.take();


                for (ServerStart.ClientHandler activeUser : ServerStart.clientList) {
                    activeUser.out.println(headOfQueue.toParcel());
                    if(headOfQueue.getAttachmentSize() != 0) {
                        activeUser.dOut.write(headOfQueue.getAttachmentByteArray());
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }



        }
    }
}
