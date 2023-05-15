package server.clientHandler;

import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.serverObject.ServerMessageFabric;
import protocol.userObject.DetachUser;
import protocol.userObject.ListUser;
import protocol.userObject.MessageUser;
import protocol.userObject.RegisterUser;

import java.util.concurrent.BlockingQueue;

public class MessageHandler {
    private final BlockingQueue<ObjectServer> queue;
    private final ServerMessageFabric fabric;

    MessageHandler(BlockingQueue<ObjectServer> queue, ServerMessageFabric fabric){
        this.queue = queue;
        this.fabric = fabric;
    }

    public void handle(ObjectUser request) throws InterruptedException {
        if(request instanceof RegisterUser r){
            queue.put(fabric.makeSuccessAnswer(request.getId()));
        } else if (request instanceof MessageUser r) {
            queue.put(fabric.makeSuccessAnswer(request.getId()));
        } else if (request instanceof DetachUser r) {
            queue.put(fabric.makeSuccessAnswer(request.getId()));
        } else if (request instanceof ListUser r) {
            queue.put(fabric.makeSuccessAnswer(request.getId()));
        } else {
            System.out.println("Unknown request type");
        }
    }
}
