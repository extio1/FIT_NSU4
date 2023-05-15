package protocol.userObject.serialization;

import protocol.ObjectServer;
import protocol.userObject.MessageUser;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class MessageUserSerialized implements Serializable, MessageUser {
    private final String data;

    public MessageUserSerialized(String message){
        super();
        data = message;
    }

    public String getData() {
        return data;
    }

    @Override
    public void handleServerResponse(ObjectServer response) {

    }
}
