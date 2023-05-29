package protocol.visitor;

import client.ClientSessionData;
import protocol.serverObject.event.AttachUserServerEv;
import protocol.serverObject.event.DetachUserServerEv;
import protocol.serverObject.event.MessageFromServerEv;
import protocol.serverObject.event.MessageFromUserEv;
import protocol.serverObject.response.ErrorAnswerServerResp;
import protocol.serverObject.response.SuccessAnswerServerResp;

public class VisitorServer {
    private final ClientSessionData data;
    public VisitorServer(ClientSessionData data){
        this.data = data;
    }
    
    public void visitAttachClient(AttachUserServerEv e) {
        data.addUserToList(e.getNickname());
    }

    
    public void visitDetachClient(DetachUserServerEv e) {
        data.removeUserFromList(e.getNickname());
    }

    
    public void visitMessageFromUser(MessageFromUserEv e) {
        data.addMessage(e.getDate(), e.getNickname(), e.getMessage());
    }

    
    public void visitServerMessage(MessageFromServerEv e) {
        data.addMessage(e.getDate(), e.getName(), e.getMessage());
    }

    
    public void visitSuccessResponse(SuccessAnswerServerResp r){
        long reqId = r.getRequestId();
        data.getWaitingRequestById(reqId).handleServerResponse(r, data);
        data.removeWaitingRequest(reqId);
    }

    public void visitErrorResponse(ErrorAnswerServerResp e){
        long reqId = e.getRequestId();
        data.addError(data.getWaitingRequestById(reqId).getClass().getSimpleName(), e.getReason());
        data.removeWaitingRequest(reqId);
    }
}


