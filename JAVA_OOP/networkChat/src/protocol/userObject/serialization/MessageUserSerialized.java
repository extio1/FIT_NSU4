package protocol.userObject.serialization;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Response;
import protocol.userObject.VisitorClient;
import protocol.userObject.request.MessageUser;

import java.io.Serializable;

public class MessageUserSerialized extends ObjectUser implements Serializable, MessageUser {
    private final String data;

    public MessageUserSerialized(String message){
        super();
        data = message;
    }
    @Override
    public String getMessage() {
        return data;
    }

    @Override
    public void accept(VisitorClient v) {
        v.visitMessageRequest(this);
    }

    @Override
    public void handleServerResponse(Response r, ClientSessionData data) {

    }
}
