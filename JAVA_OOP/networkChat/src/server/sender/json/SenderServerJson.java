package server.sender.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import protocol.ObjectServer;
import protocol.Request;
import protocol.Response;
import protocol.clientObject.request.DetachUserReq;
import protocol.clientObject.request.ListUserReq;
import protocol.clientObject.request.MessageUserReq;
import protocol.clientObject.request.RegisterUserReq;
import protocol.json.deserializer.JsonDeserializerObjUser;
import protocol.json.serializer.JsonSerializerObjServer;
import protocol.serverObject.event.AttachUserServerEv;
import protocol.serverObject.event.DetachUserServerEv;
import protocol.serverObject.event.MessageFromServerEv;
import protocol.serverObject.event.MessageFromUserEv;
import protocol.serverObject.response.ErrorAnswerServerResp;
import protocol.serverObject.response.SuccessAnswerServerResp;
import server.ServerSessionContext;
import server.logger.LoggerServer;
import server.sender.SenderServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

public class SenderServerJson extends SenderServer {
    private final BufferedWriter out;
    private final Gson gson;

    public SenderServerJson(BlockingQueue<ObjectServer> queue, BufferedWriter out,
                                  ServerSessionContext context, LoggerServer logger){
        super(queue, context, logger);
        this.out = out;

        JsonSerializerObjServer serializer = new JsonSerializerObjServer();
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(new TypeToken<ErrorAnswerServerResp>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<SuccessAnswerServerResp>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<AttachUserServerEv>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<DetachUserServerEv>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<MessageFromServerEv>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<MessageFromUserEv>(){}.getType(), serializer);

        gson = builder.create();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                ObjectServer send =  queue.take();

                if (send instanceof Response r) {
                    logger.makeLog(Level.INFO,
                            "Send response to <" + context.getUserName() + ">\ntype:" + send +
                                    "\n(time" + send.getDate() + ")" + "\n(" + (r.getRequestId() + "id)"));
                } else {
                    logger.makeLog(Level.INFO,
                            "Server event\ntype: " + send + "\n(time" + send.getDate() + ")");
                }

                out.write(gson.toJson(send)+'\n');
                out.flush();
            } catch (IOException e) {
                System.out.println("Sndr srv "+e.getMessage());
            } catch (InterruptedException e){
                break;
            }
        }
    }
}
