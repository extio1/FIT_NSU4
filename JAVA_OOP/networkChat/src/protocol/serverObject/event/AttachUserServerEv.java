package protocol.serverObject.event;

import protocol.Event;
import protocol.ObjectServer;
import protocol.visitor.VisitorServer;

public class AttachUserServerEv extends ObjectServer implements Event {
    private final String name;
    public AttachUserServerEv(String date, String name){
        super(date);
        this.name = name;
    }

    public String getNickname() {
        return name;
    }

    public void accept(VisitorServer visitorServer) {
        visitorServer.visitAttachClient(this);
    }
}
