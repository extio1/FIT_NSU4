package protocol.serverObject;

import protocol.serverObject.response.ErrorAnswerServer;
import protocol.serverObject.response.SuccessAnswerServer;

public interface ServerMessageFabric {
    DetachedUserServer makeDetachedUser();
    ErrorAnswerServer makeErrorAnswer();
    ServerMessageFabric makeServerMessage();
    SuccessAnswerServer makeSuccessAnswer(long succeedId);
}
