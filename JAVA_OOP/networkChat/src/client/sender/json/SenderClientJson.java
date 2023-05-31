package client.sender.json;

import client.ClientSessionData;
import client.sender.SenderClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import protocol.ObjectUser;
import protocol.Request;
import protocol.clientObject.request.DetachUserReq;
import protocol.clientObject.request.ListUserReq;
import protocol.clientObject.request.MessageUserReq;
import protocol.clientObject.request.RegisterUserReq;
import protocol.json.serializer.JsonSerializerObjUser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class SenderClientJson extends SenderClient {
    private final BufferedWriter out;
    private final Gson gson;

    public SenderClientJson(BlockingQueue<Request> queue, ClientSessionData data, BufferedWriter out){
        super(queue, data);
        this.out = out;

        JsonSerializer<ObjectUser> serializer = new JsonSerializerObjUser();
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(new TypeToken<DetachUserReq>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<ListUserReq>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<MessageUserReq>(){}.getType(), serializer);
        builder.registerTypeAdapter(new TypeToken<RegisterUserReq>(){}.getType(), serializer);

        gson = builder.create();
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Request send = queue.take();
                data.addWaitingRequest(send);

                out.write(gson.toJson(send)+'\n');
                out.flush();
            } catch (IOException e) {
                System.out.println("Sndr clt "+e.getMessage());
            } catch (InterruptedException e){
                break;
            }
        }
    }
}
