package protocol.clientObject.request;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Request;
import protocol.Response;
import protocol.visitor.VisitorClient;


public class MessageUserReq extends ObjectUser implements Request {
    private final String data;
    public MessageUserReq(String message){
        super();
        data = message;
    }
    public String getMessage() {
        return data;
    }
    public void accept(VisitorClient v) {
        v.visitMessageRequest(this);
    }
    public void handleServerResponse(Response r, ClientSessionData data) {}
}
