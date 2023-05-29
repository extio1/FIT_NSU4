package client.sender;

import client.ClientSessionData;
import client.ImplementationClient;
import client.sender.java.SenderClientJava;
import client.sender.json.SenderClientJson;
import exception.UnknownImplementationException;
import protocol.Request;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public abstract class SenderClient extends Thread{
    protected final BlockingQueue<Request> queue;
    protected final ClientSessionData data;
    protected SenderClient(BlockingQueue<Request> queue, ClientSessionData data){
        this.queue = queue;
        this.data = data;
        this.setName("To server sender: client thread");
    }
    public static SenderClient makeSender(BlockingQueue<Request> queue, ClientSessionData data,
                                          Socket socket, ImplementationClient impl)
            throws IOException, UnknownImplementationException
    {
        switch(impl){
            case JAVA -> {
                return new SenderClientJava(queue, data, new ObjectOutputStream(socket.getOutputStream()));
            }
            case JSON -> {
                return new SenderClientJson(queue, data,
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
            }
        }

        throw new UnknownImplementationException(impl.name());
    }
}
