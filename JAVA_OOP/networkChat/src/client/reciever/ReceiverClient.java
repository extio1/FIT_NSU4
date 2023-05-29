package client.reciever;

import client.ImplementationClient;
import client.reciever.java.ReceiverClientJava;
import client.reciever.json.ReceiverClientJson;
import exception.UnknownImplementationException;
import protocol.visitor.VisitorServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public abstract class ReceiverClient extends Thread{
    protected final VisitorServer visitorServer;
    protected ReceiverClient(VisitorServer visitorClient){
        this.visitorServer = visitorClient;
        this.setName("Server lister: client thread");
    }
    public static ReceiverClient makeReceiver(VisitorServer v, Socket socket, ImplementationClient impl) throws IOException, UnknownImplementationException {
        switch (impl){
            case JAVA -> {
                return new ReceiverClientJava(v, new ObjectInputStream(socket.getInputStream()));
            }
            case JSON -> {
                return new ReceiverClientJson(v,
                        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)));
            }
        }
        throw new UnknownImplementationException(impl.name());
    }
}
