package client.reciever.json;

import client.reciever.ReceiverClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import protocol.Event;
import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.Response;
import protocol.json.deserializer.JsonDeserializerObjServer;
import protocol.json.serializer.JsonSerializerObjServer;
import protocol.serverObject.event.AttachUserServerEv;
import protocol.serverObject.event.DetachUserServerEv;
import protocol.serverObject.event.MessageFromServerEv;
import protocol.serverObject.event.MessageFromUserEv;
import protocol.serverObject.response.ErrorAnswerServerResp;
import protocol.serverObject.response.SuccessAnswerServerResp;
import protocol.visitor.VisitorServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;


public class ReceiverClientJson extends ReceiverClient {
    private final BufferedReader in;
    private final Gson gson;

    public ReceiverClientJson(VisitorServer visitorServer, BufferedReader in) {
        super(visitorServer);
        this.in = in;

        JsonDeserializerObjServer deserializer = new JsonDeserializerObjServer();
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(new TypeToken<ObjectServer>(){}.getType(), deserializer);
        gson = builder.create();
    }



    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                String srvMess = in.readLine();
                if(srvMess != null) {
                    ObjectServer srv = gson.fromJson(srvMess, ObjectServer.class);
                    srv.accept(visitorServer);
                }
            } catch (IOException e) {
                System.out.println("Receiver clt "+e.getMessage());
                if(e instanceof SocketException){
                    break;
                }
            }
        }
    }
}
