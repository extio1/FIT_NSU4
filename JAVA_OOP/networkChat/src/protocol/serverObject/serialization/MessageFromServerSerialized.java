package protocol.serverObject.serialization;

import protocol.ObjectServer;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.event.MessageFromServer;

public class MessageFromServerSerialized extends ObjectServer implements MessageFromServer {
    private final String message;

    public MessageFromServerSerialized(String date, String message){
        super(date);
        this.message = message;
    }

    @Override
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitServerMessage(this);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
