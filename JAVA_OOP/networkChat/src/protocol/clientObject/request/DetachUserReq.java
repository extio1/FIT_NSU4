package protocol.clientObject.request;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Request;
import protocol.Response;
import protocol.visitor.VisitorClient;

public class DetachUserReq extends ObjectUser implements Request {
    private final boolean isTimeout;
    public DetachUserReq(Boolean isTimeout){
        super();
        this.isTimeout = isTimeout;
    }
    public void accept(VisitorClient v) {
        v.visitDetachUserRequest(this);
    }
    public void handleServerResponse(Response r, ClientSessionData data) {}
    public boolean isTimeout() {
        return isTimeout;
    }
}
