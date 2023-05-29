package protocol.clientObject.request;

import client.ClientSessionData;
import protocol.ObjectUser;
import protocol.Request;
import protocol.Response;
import protocol.serverObject.response.ErrorAnswerServerResp;
import protocol.serverObject.response.SuccessAnswerServerResp;
import protocol.visitor.VisitorClient;

public class ListUserReq extends ObjectUser implements Request {
    public ListUserReq(){
        super();
    }
    public void accept(VisitorClient v) {
        v.visitListUserRequest(this);
    }
    public void handleServerResponse(Response r, ClientSessionData data) {
        if(r instanceof SuccessAnswerServerResp s){
            data.setUserList(s.getUserList());
        } else if(r instanceof ErrorAnswerServerResp e){
            data.addError(e.toString(), e.getReason());
        }
    }

}
