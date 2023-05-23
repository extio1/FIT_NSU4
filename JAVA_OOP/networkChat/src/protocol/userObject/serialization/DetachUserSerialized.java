package protocol.userObject.serialization;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Response;
import protocol.userObject.VisitorClient;
import protocol.userObject.request.DetachUser;

import java.io.Serializable;

public class DetachUserSerialized extends ObjectUser implements Serializable, DetachUser {
    private boolean isTimeout;
    public DetachUserSerialized(boolean isTimeout){
        super();
        this.isTimeout = isTimeout;
    }

    @Override
    public void accept(VisitorClient v) {
        v.visitDetachUserRequest(this);
    }

    @Override
    public void handleServerResponse(Response r, ClientSessionData data) {

    }

    @Override
    public boolean isTimeout() {
        return isTimeout;
    }
}
