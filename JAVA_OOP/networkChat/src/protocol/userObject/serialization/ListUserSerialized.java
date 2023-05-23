package protocol.userObject.serialization;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Response;
import protocol.serverObject.response.ErrorAnswerServer;
import protocol.serverObject.response.SuccessAnswerServer;
import protocol.userObject.VisitorClient;
import protocol.userObject.request.ListUser;

import java.io.Serializable;

public class ListUserSerialized extends ObjectUser implements Serializable, ListUser {
    public ListUserSerialized(){
        super();
    }

    @Override
    public void accept(VisitorClient v) {
        v.visitListUserRequest(this);
    }

    @Override
    public void handleServerResponse(Response r, ClientSessionData data) {
        if(r instanceof SuccessAnswerServer s){
            data.setUserList(s.getUserList());
        } else if(r instanceof ErrorAnswerServer e){
            data.addError(e.toString(), e.getReason());
        }
    }

}
