package protocol.serverObject.fabric;

import protocol.serverObject.DetachedUserServer;
import protocol.serverObject.ErrorAnswerServer;
import protocol.serverObject.ServerMessageFabric;
import protocol.serverObject.SuccessAnswerServer;

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
    public SuccessAnswerServer makeSuccessAnswer() {
        return null;
    }
}
