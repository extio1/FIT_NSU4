package protocol.clientObject.request;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Request;
import protocol.Response;
import protocol.serverObject.response.SuccessAnswerServerResp;
import protocol.visitor.VisitorClient;

import java.io.Serializable;

public class RegisterUserReq extends ObjectUser implements Request {
    private final String data;
    public RegisterUserReq(String name){
        super();
        data = name;
    }
    public String getName() {
        return data;
    }
    public void accept(VisitorClient v) {
        v.visitRegisterUserRequest(this);
    }
    public void handleServerResponse(Response r, ClientSessionData data) {
        if(r instanceof SuccessAnswerServerResp s){
            if(s.getChatHistory() != null)
                data.setChatHistory(s.getChatHistory());
            if(s.getUserName() != null)
                data.setClientName(s.getUserName());
        }
    }
}
