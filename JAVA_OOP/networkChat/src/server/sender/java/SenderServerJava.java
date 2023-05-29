package server.sender.java;

import protocol.ObjectServer;
import protocol.Response;
import server.ServerSessionContext;
import server.logger.LoggerServer;
import server.sender.SenderServer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;


public class SenderServerJava extends SenderServer {
    private final ObjectOutputStream out;
    public SenderServerJava(BlockingQueue<ObjectServer> queue, ObjectOutputStream out,
                            ServerSessionContext context, LoggerServer logger)
    {
        super(queue, context, logger);
        this.out = out;
    }

    @Override
    public void run(){
        while(!Thread.interrupted()){
            try {
                ObjectServer send = queue.take();

                if (send instanceof Response r) {
                    logger.makeLog(Level.INFO,
                            "Send response to <" + context.getUserName() + ">\ntype:" + send +
                                    "\n(time" + send.getDate() + ")" + "\n(" + (r.getRequestId() + "id)"));
                } else {
                    logger.makeLog(Level.INFO,
                            "Server event\ntype: " + send + "\n(time" + send.getDate() + ")");
                }

                out.writeObject(send);
            } catch (IOException e) {
                System.out.println("Sndr srv "+e.getMessage());
            } catch (InterruptedException e){
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}

