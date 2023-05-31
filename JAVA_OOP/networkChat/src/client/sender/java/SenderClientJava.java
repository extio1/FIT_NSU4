package client.sender.java;

import client.ClientSessionData;
import client.sender.SenderClient;
import protocol.Request;
import protocol.clientObject.request.DetachUserReq;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;

public class SenderClientJava extends SenderClient {

    private final ObjectOutputStream out;

    public SenderClientJava(BlockingQueue<Request> queue, ClientSessionData data, ObjectOutputStream out) {
        super(queue, data);
        this.out = out;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Request send = queue.take();
                data.addWaitingRequest(send);
                out.writeObject(send);
            } catch (InterruptedException e){
                break;
            } catch (IOException e) {
                System.out.println("Sndr clt "+e.getMessage());
            }
        }
    }
}
