package protocol.userObject.serialization;

import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.userObject.DetachUser;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class DetachUserSerialized extends ObjectUser implements Serializable, DetachUser {
    public DetachUserSerialized(){
        super();
    }

    @Override
    public void handleServerResponse(ObjectServer response) {

    }
}
