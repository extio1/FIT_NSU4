package protocol.serverObject.fabric;

import protocol.serverObject.DetachedUserServer;
import protocol.serverObject.response.ErrorAnswerServer;
import protocol.serverObject.ServerMessageFabric;
import protocol.serverObject.response.SuccessAnswerServer;

public class ServerSerializedFabric implements ServerMessageFabric {
    @Override
    public DetachedUserServer makeDetachedUser() {
        return null;
    }

    @Override
    public ErrorAnswerServer makeErrorAnswer() {
        return null;
    }

    @Override
    public ServerMessageFabric makeServerMessage() {
        return null;
    }

    @Override
    public SuccessAnswerServer makeSuccessAnswer(long succeedId) {
        return null;
    }
}
