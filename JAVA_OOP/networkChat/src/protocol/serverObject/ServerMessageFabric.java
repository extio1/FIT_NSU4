package protocol.serverObject;

import protocol.serverObject.event.DetachUserServer;
import protocol.serverObject.event.MessageFromServer;
import protocol.serverObject.event.MessageFromUser;
import protocol.serverObject.event.AttachUserServer;
import protocol.serverObject.response.ErrorAnswerServer;
import protocol.serverObject.response.SuccessAnswerServer;

public interface ServerMessageFabric {
    DetachUserServer makeDetachedUserEvent(String name);
    ErrorAnswerServer makeErrorAnswer(long id, String reason);
    MessageFromUser makeServerMessageFromUserEvent(String name, String message);
    SuccessAnswerServer makeSuccessAnswer(long id);
    AttachUserServer makeAttachUserEvent(String name);

    MessageFromServer makeMessageFromServer(String message);
}
