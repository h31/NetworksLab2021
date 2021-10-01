package runnable;

import model.ExchangeFormat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
                        byte[] bytes = headOfQueue.getAttachmentByteArray();
                        for(int i=0;i<headOfQueue.getAttachmentSize();i++){
                            activeUser.dOut.writeByte(bytes[i]);
                        }
                        System.out.println("сервер отправил все клиентам");
                        /*activeUser.dOut.write(headOfQueue.getAttachmentByteArray());
                        activeUser.dOut.flush();*/
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }



        }
    }
}
