package server.reciever;

import exception.UnknownImplementationException;
import protocol.ObjectServer;
import protocol.visitor.VisitorClient;
import server.ImplementationServer;
import server.ServerSessionContext;
import server.logger.LoggerServer;
import server.reciever.java.ReceiverServerJava;
import server.reciever.json.ReceiverServerJson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public abstract class ReceiverServer extends Thread{
    protected final BlockingQueue<ObjectServer> queue;
    protected final ServerSessionContext context;
    protected final LoggerServer logger;
    protected final VisitorClient visitor;

    protected ReceiverServer(BlockingQueue<ObjectServer> queue, ServerSessionContext context,
                          LoggerServer logger, VisitorClient visitor)
    {
        this.queue = queue;
        this.context = context;
        this.logger = logger;
        this.visitor = visitor;
        this.setName("User listener: server thread");
    }
    public abstract void close() throws IOException;
    public static ReceiverServer makeReceiver(BlockingQueue<ObjectServer> queue, ServerSessionContext context,
                                              LoggerServer logger, VisitorClient visitor, Socket sock,
                                              ImplementationServer impl)
            throws IOException, UnknownImplementationException {
        switch (impl){
            case JAVA -> {
                return new ReceiverServerJava(queue, context, logger,
                        visitor, new ObjectInputStream(sock.getInputStream())
                );
            }
            case JSON -> {
                return new ReceiverServerJson(queue, context, logger,
                        visitor,
                        new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8)));
            }
        }
        throw new UnknownImplementationException(impl.name());
    }
}
