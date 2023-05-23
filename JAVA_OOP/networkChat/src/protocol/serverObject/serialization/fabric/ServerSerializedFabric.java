package protocol.serverObject.serialization.fabric;

import protocol.serverObject.event.DetachUserServer;
import protocol.serverObject.event.MessageFromServer;
import protocol.serverObject.event.MessageFromUser;
import protocol.serverObject.event.AttachUserServer;
import protocol.serverObject.response.ErrorAnswerServer;
import protocol.serverObject.ServerMessageFabric;
import protocol.serverObject.response.SuccessAnswerServer;
import protocol.serverObject.serialization.*;

import java.text.SimpleDateFormat;

public class ServerSerializedFabric implements ServerMessageFabric {
    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    public DetachUserServer makeDetachedUserEvent(String name) {
        return new DetachUserServerSerialized(format.format(System.currentTimeMillis()), name);
    }

    @Override
    public ErrorAnswerServer makeErrorAnswer(long id, String reason) {
        return new ErrorAnswerServerSerialized(format.format(System.currentTimeMillis()), id, reason);
    }

    @Override
    public MessageFromUser makeServerMessageFromUserEvent(String name, String message) {
        return new MessageFromUserSerialized(format.format(System.currentTimeMillis()), name, message);
    }

    @Override
    public SuccessAnswerServer makeSuccessAnswer(long succeedId) {
        return new SuccessAnswerServerSerialized(format.format(System.currentTimeMillis()), succeedId);
    }

    @Override
    public AttachUserServer makeAttachUserEvent(String name) {
        return new AttachUserServerSerialized(format.format(System.currentTimeMillis()), name);
    }

    @Override
    public MessageFromServer makeMessageFromServer(String message) {
        return new MessageFromServerSerialized(format.format(System.currentTimeMillis()), message);
    }
}
