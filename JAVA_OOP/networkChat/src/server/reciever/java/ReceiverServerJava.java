package server.reciever.java;

import protocol.ObjectServer;
import protocol.Request;
import protocol.visitor.VisitorClient;
import server.ServerSessionContext;
import server.logger.LoggerServer;
import server.reciever.ReceiverServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

public class ReceiverServerJava extends ReceiverServer {
    private final ObjectInputStream in;
    public ReceiverServerJava(BlockingQueue<ObjectServer> queue, ServerSessionContext context,
                              LoggerServer logger, VisitorClient visitor, ObjectInputStream in){
        super(queue, context, logger, visitor);
        this.in = in;
    }

    @Override
    public void run(){
        while(!Thread.interrupted()){
            try {
                Request userReq = (Request) in.readObject();

                logger.makeLog(Level.INFO,
                        "Request from <"+context.getUserName()+">\ntype:"+
                                userReq+"\n("+userReq.getId()+" id request)");

                userReq.accept(visitor);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Rcvr srv "+e.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
