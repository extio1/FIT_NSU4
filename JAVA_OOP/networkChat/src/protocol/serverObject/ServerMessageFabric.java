package protocol.serverObject;

public interface ServerMessageFabric {
    DetachedUserServer makeDetachedUser();
    ErrorAnswerServer makeErrorAnswer();
    ServerMessageFabric makeServerMessage();
    SuccessAnswerServer makeSuccessAnswer();
}
