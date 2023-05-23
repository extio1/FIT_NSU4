package server.clientHandler.json;

import protocol.ObjectServer;
import protocol.Request;
import protocol.Response;
import protocol.userObject.VisitorClient;
import protocol.userObject.serialization.DetachUserSerialized;
import protocol.userObject.serialization.visitor.VisitorClientSerialized;
import server.ClientHandler;
import server.Server;
import server.clientHandler.ServerSessionContext;
import server.logger.LoggerServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class ClientHandlerJson implements ClientHandler {
    private final Sender sender;
    private final UserListener userListener;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final ServerSessionContext context;
    private final BlockingQueue<ObjectServer> toSendQueue;
    private final VisitorClient visitorClient;
    private final Server server;
    private final LoggerServer logger;
    public ClientHandlerJson(Socket socket, Server server, LoggerServer logger) throws IOException {
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        this.logger = logger;
        this.server = server;
        toSendQueue = new LinkedBlockingQueue<>();

        sender = new Sender();
        userListener = new UserListener();

        context = new ServerSessionContext();
        visitorClient = new VisitorClientSerialized(this, server);

        userListener.setName("User listener: server thread");
        sender.setName("To user sender: server thread");
        sender.start();
        userListener.start();

        logger.makeLog(Level.INFO,
                "New client accepted, working on socket port "+socket.getPort());
    }

    @Override
    public void send(ObjectServer msg) {
        toSendQueue.add(msg);
    }

    public ServerSessionContext getContext(){
        return context;
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
                    Request userReq = (Request) in.readObject();

                    logger.makeLog(Level.INFO,
                            "Request from <"+context.getUserName()+">\ntype:"+
                                    userReq+"\n("+userReq.getId()+" id request)");

                    userReq.accept(visitorClient);
                } catch (IOException e) {
                    new DetachUserSerialized(true).accept(visitorClient);
                    shutdown();
                } catch (ClassNotFoundException e){
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class Sender extends Thread{
        @Override
        public void run(){
            while(!Thread.interrupted()){
                try {
                    ObjectServer send = toSendQueue.take();

                    if(send instanceof Response r)
                        logger.makeLog(Level.INFO,
                                "Send response to <"+context.getUserName()+">\ntype:"+send+
                                        "\n(time"+send.getDate()+")"+"\n("+(r.getRequestId()+"id)"));
                    else
                        logger.makeLog(Level.INFO,
                                "Server event\ntype: "+send+"\n(time"+send.getDate()+")");

                    out.writeObject(send);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (InterruptedException e){
                    break;
                }
            }
        }
    }
}
