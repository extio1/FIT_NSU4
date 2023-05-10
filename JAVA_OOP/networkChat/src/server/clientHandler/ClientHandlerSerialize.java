package server.clientHandler;

import protocol.serializationProtocol.SerializationProtocolObject;
import server.ClientHandler;
import server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandlerSerialize extends ClientHandler {
    private final Server server;
    private final Socket socket;

    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private final int session_id;

    public ClientHandlerSerialize(Socket socket, Server server, int id) throws IOException {
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        this.server = server;
        this.socket = socket;
        this.session_id = id;
        this.start();
    }

    @Override
    public void run(){
        while(!Thread.interrupted()){
            try {
                SerializationProtocolObject word = (SerializationProtocolObject) in.readObject();
                server.doBroadcast(word);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    @Override
    public void send(Object msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e){
            System.out.println("Error while sending "+msg+"\n"+e.getMessage());
        }
    }
}
