package protocol.serverObject.serialization.visitor;

import client.ClientSessionData;
import protocol.Response;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.event.AttachUserServer;
import protocol.serverObject.event.DetachUserServer;
import protocol.serverObject.event.MessageFromServer;
import protocol.serverObject.event.MessageFromUser;
import protocol.serverObject.response.ErrorAnswerServer;
import protocol.serverObject.response.SuccessAnswerServer;

public class VisitorServerSerialize implements VisitorServer {
    private final ClientSessionData data;
    public VisitorServerSerialize(ClientSessionData data){
        this.data = data;
    }
    @Override
    public void visitAttachClient(AttachUserServer e) {
        data.addUserToList(e.getNickname());
    }

    @Override
    public void visitDetachClient(DetachUserServer e) {
        data.removeUserFromList(e.getNickname());
    }

    @Override
    public void visitMessageFromUser(MessageFromUser e) {
        data.addMessage(e.getDate(), e.getNickname(), e.getMessage());
    }

    @Override
    public void visitServerMessage(MessageFromServer e) {
        data.addMessage(e.getDate(), "  !SERVER!  ", e.getMessage());
    }

    @Override
    public void visitResponse(Response r){
        long reqId = r.getRequestId();
        if(r instanceof ErrorAnswerServer e)
            data.addError(data.getWaitingRequestById(reqId).getClass().getSimpleName(), e.getReason());
        else if(r instanceof SuccessAnswerServer)
            data.getWaitingRequestById(reqId).handleServerResponse(r, data);

        data.removeWaitingRequest(reqId);
    }
}


