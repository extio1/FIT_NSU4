package server;

import exception.UnknownImplementationException;
import protocol.Event;
import protocol.ObjectServer;
import protocol.serverObject.ServerObjFabric;
import protocol.serverObject.event.DetachUserServerEv;
import protocol.serverObject.event.MessageFromServerEv;
import protocol.visitor.VisitorClient;
import server.reciever.ReceiverServer;
import server.sender.SenderServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class ClientHandler {
    private final SenderServer sender;
    private final ReceiverServer receiver;
    private final ServerSessionContext context = new ServerSessionContext();
    private final BlockingQueue<ObjectServer> toSendQueue = new LinkedBlockingQueue<>();
    private final Server server;
    private final Ping ping;
    private final Socket socket;
    private final Socket echoSocket;

    public ClientHandler(Socket socket, Socket echoSocket, Server server, ImplementationServer impl)
            throws IOException, UnknownImplementationException
    {
        this.server = server;
        this.socket = socket;
        this.echoSocket = echoSocket;

        receiver = ReceiverServer.makeReceiver(toSendQueue, context, server.getLogger(),
                new VisitorClient(this, server), socket, impl);
        sender = SenderServer.makeSender(toSendQueue, socket, context, server.getLogger(), impl);

        ping = new Ping(echoSocket);

        sender.start();
        receiver.start();
        ping.start();

        server.getLogger().makeLog(Level.INFO,
                "New client accepted, working on socket port "+socket.getPort());
    }

    public void send(ObjectServer msg) {
        toSendQueue.add(msg);
    }
    public ServerSessionContext getContext(){
        return context;
    }

    public void shutdown() {
        try {
            sender.interrupt();
            receiver.interrupt();
            ping.interrupt();

            socket.close();
            echoSocket.close();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private class Ping extends Thread{
        private final Socket sock;
        public Ping(Socket sock) {
            this.sock = sock;
            this.setName("Ping: server handler");
        }

        @Override
        public void run() {
            InputStream in = null;
            OutputStream out = null;
            try {
                 in = sock.getInputStream();
                 out = sock.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            while (!Thread.interrupted()) {
                try {
                    int obj = in.read();
                    out.write(obj);
                } catch (IOException e) {
                    if(e instanceof SocketTimeoutException) {
                        ServerObjFabric fabric = new ServerObjFabric();

                        server.getContext().removeUser(context.getUserName());

                        server.doBroadcast(fabric.makeDetachedUserEvent(context.getUserName()));

                        MessageFromServerEv ev = fabric.makeMessageFromServer(context.getUserName() + " left(timeout)");
                        server.getContext().addMsgServer(ev.getDate(), context.getUserName() + " left(timeout)");
                        server.doBroadcast(ev);

                        sender.interrupt();
                        receiver.interrupt();
                        try {
                            socket.close();
                            echoSocket.close();
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                        break;
                    }
                }
            }
        }
    }
}
