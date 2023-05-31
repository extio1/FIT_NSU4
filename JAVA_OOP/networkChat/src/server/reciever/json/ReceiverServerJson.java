package server.reciever.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.clientObject.request.DetachUserReq;
import protocol.clientObject.request.ListUserReq;
import protocol.clientObject.request.MessageUserReq;
import protocol.clientObject.request.RegisterUserReq;
import protocol.json.deserializer.JsonDeserializerObjUser;
import protocol.visitor.VisitorClient;
import protocol.visitor.VisitorServer;
import server.ServerSessionContext;
import server.logger.LoggerServer;
import server.reciever.ReceiverServer;

import java.io.*;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;


public class ReceiverServerJson extends ReceiverServer {
    private final BufferedReader in;
    private final Gson gson;
    public ReceiverServerJson(BlockingQueue<ObjectServer> queue, ServerSessionContext context,
                              LoggerServer logger, VisitorClient visitor, BufferedReader in){
        super(queue, context, logger, visitor);
        this.in = in;

        JsonDeserializerObjUser deserializer = new JsonDeserializerObjUser();
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(new TypeToken<ObjectUser>(){}.getType(), deserializer);

        gson = builder.create();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                String mess = in.readLine();
                ObjectUser obj = gson.fromJson(mess, ObjectUser.class);
                if(obj != null)
                    obj.accept(visitor);
            } catch (IOException e) {
                System.out.println("Rcvr srv "+e.getMessage());
                if(e instanceof SocketException){
                    break;
                }
            }
        }
    }
}
