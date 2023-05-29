package protocol.serverObject;

import protocol.serverObject.event.AttachUserServerEv;
import protocol.serverObject.event.DetachUserServerEv;
import protocol.serverObject.event.MessageFromServerEv;
import protocol.serverObject.event.MessageFromUserEv;
import protocol.serverObject.response.ErrorAnswerServerResp;
import protocol.serverObject.response.SuccessAnswerServerResp;

import java.text.SimpleDateFormat;

public class ServerObjFabric {
    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
   
    public DetachUserServerEv makeDetachedUserEvent(String name) {
        return new DetachUserServerEv(format.format(System.currentTimeMillis()), name);
    }

    public ErrorAnswerServerResp makeErrorAnswer(long id, String reason) {
        return new ErrorAnswerServerResp(format.format(System.currentTimeMillis()), id, reason);
    }

    public MessageFromUserEv makeServerMessageFromUserEvent(String name, String message) {
        return new MessageFromUserEv(format.format(System.currentTimeMillis()), name, message);
    }

    public SuccessAnswerServerResp makeSuccessAnswer(long succeedId) {
        return new SuccessAnswerServerResp(format.format(System.currentTimeMillis()), succeedId);
    }
   
    public AttachUserServerEv makeAttachUserEvent(String name) {
        return new AttachUserServerEv(format.format(System.currentTimeMillis()), name);
    }

    public MessageFromServerEv makeMessageFromServer(String message) {
        return new MessageFromServerEv(format.format(System.currentTimeMillis()), message);
    }
}
