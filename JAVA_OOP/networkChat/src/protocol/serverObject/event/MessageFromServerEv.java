package protocol.serverObject.event;

import protocol.Event;
import protocol.ObjectServer;
import protocol.visitor.VisitorServer;

public class MessageFromServerEv extends ObjectServer implements Event {
    private final String message;

    public MessageFromServerEv(String date, String message){
        super(date);
        this.message = message;
    }
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitServerMessage(this);
    }
    public String getName(){
            return "SERVER";
    }
    public String getMessage() {
        return message;
    }
}
