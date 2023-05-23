package protocol.serverObject;

import protocol.Response;
import protocol.serverObject.event.AttachUserServer;
import protocol.serverObject.event.DetachUserServer;
import protocol.serverObject.event.MessageFromServer;
import protocol.serverObject.event.MessageFromUser;

public interface VisitorServer {
    void visitAttachClient(AttachUserServer e);
    void visitDetachClient(DetachUserServer e);
    void visitMessageFromUser(MessageFromUser e);
    void visitServerMessage(MessageFromServer e);
    void visitResponse(Response r);


}
