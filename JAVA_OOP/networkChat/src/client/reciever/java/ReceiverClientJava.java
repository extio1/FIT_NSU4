package client.reciever.java;

import client.reciever.ReceiverClient;
import protocol.Event;
import protocol.ObjectServer;
import protocol.Response;
import protocol.visitor.VisitorServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;

public class ReceiverClientJava extends ReceiverClient {
    private final ObjectInputStream in;
    public ReceiverClientJava(VisitorServer visitorServer, ObjectInputStream in){
        super(visitorServer);
        this.in = in;
    }
    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                ObjectServer srvMess = (ObjectServer) in.readObject();
                srvMess.accept(visitorServer);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Receiver clt "+e.getMessage());
            }
        }
    }

}
