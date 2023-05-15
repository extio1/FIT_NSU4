package protocol.userObject.serialization;

import protocol.ObjectServer;
import protocol.userObject.ListUser;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class ListUserSerialized implements Serializable, ListUser {
    public ListUserSerialized(){
        super();
    }

    @Override
    public void handleServerResponse(ObjectServer response) {

    }
}
