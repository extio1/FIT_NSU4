package server.clientHandler;

import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.serverObject.ServerMessageFabric;
import protocol.serverObject.fabric.ServerSerializedFabric;
import protocol.userObject.DetachUser;
import protocol.userObject.ListUser;
import protocol.userObject.MessageUser;
import protocol.userObject.RegisterUser;
import server.ClientHandler;
import server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandlerSerialize implements ClientHandler {
    private final Server server;
    private final Socket socket;

    private final Sender sender;
    private final UserListener userListener;

    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private final BlockingQueue<ObjectServer> toSendQueue;

    private final MessageHandler messageHandler;

    private final ServerMessageFabric fabric;

    private final int session_id;

    public ClientHandlerSerialize(Socket socket, Server server, int id) throws IOException {
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        toSendQueue = new LinkedBlockingQueue<>();
        fabric = new ServerSerializedFabric();
        messageHandler = new MessageHandler(toSendQueue, fabric);

        sender = new Sender();
        userListener = new UserListener();

        this.server = server;
        this.socket = socket;
        this.session_id = id;

        sender.start();
        userListener.start();

        System.out.println("Client handler created "+socket.getInetAddress()+" port "+socket.getPort());
    }

    @Override
    public void send(ObjectServer msg) {
        toSendQueue.add(msg);
    }

    @Override
    public void shutdown() {
        try {
            sender.interrupt();
            userListener.interrupt();

            in.close();
            out.close();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private class UserListener extends Thread{
        @Override
        public void run(){
            while(!Thread.interrupted()){
                try {
                    messageHandler.handle((ObjectUser) in.readObject());
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private class Sender extends Thread{
        @Override
        public void run(){
            while(!Thread.interrupted()){
                try {
                    out.writeObject(toSendQueue.take());
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
