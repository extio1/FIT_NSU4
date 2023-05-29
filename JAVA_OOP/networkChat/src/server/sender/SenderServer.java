package server.sender;

import exception.UnknownImplementationException;
import protocol.ObjectServer;
import server.ImplementationServer;
import server.ServerSessionContext;
import server.logger.LoggerServer;
import server.sender.java.SenderServerJava;
import server.sender.json.SenderServerJson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public abstract class SenderServer extends Thread {
    protected final BlockingQueue<ObjectServer> queue;
    protected final ServerSessionContext context;
    protected final LoggerServer logger;

    protected SenderServer(BlockingQueue<ObjectServer> queue, ServerSessionContext context, LoggerServer logger)
    {
        this.queue = queue;
        this.context = context;
        this.logger = logger;
        this.setName("To user sender: server thread");
    }

    public abstract void close() throws IOException;
    public static SenderServer makeSender(BlockingQueue<ObjectServer> queue, Socket sock,
                                          ServerSessionContext context, LoggerServer logger, ImplementationServer impl)
            throws IOException, UnknownImplementationException
    {
        switch(impl){
            case JAVA -> {
                return new SenderServerJava(queue, new ObjectOutputStream(sock.getOutputStream()), context, logger);
            }
            case JSON -> {
                return new SenderServerJson(queue,
                        new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8)),
                        context, logger);
            }
        }

        throw new UnknownImplementationException(impl.name());
    }
}
