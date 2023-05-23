package protocol.userObject.serialization;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Response;
import protocol.serverObject.response.SuccessAnswerServer;
import protocol.userObject.VisitorClient;
import protocol.userObject.request.RegisterUser;

import java.io.Serializable;

public class RegisterUserSerialized extends ObjectUser implements Serializable, RegisterUser {
    private final String data;

    public RegisterUserSerialized(String name){
        super();
        data = name;
    }

    public String getName() {
        return data;
    }
    @Override
    public void accept(VisitorClient v) {
        v.visitRegisterUserRequest(this);
    }

    @Override
    public void handleServerResponse(Response r, ClientSessionData data) {
        if(r instanceof SuccessAnswerServer s){
            if(s.getChatHistory() != null)
                data.setChatHistory(s.getChatHistory());
            if(s.getUserName() != null)
                data.setClientName(s.getUserName());
        }
    }
}
