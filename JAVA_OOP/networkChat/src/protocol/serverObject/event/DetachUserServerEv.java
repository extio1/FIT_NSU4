package protocol.serverObject.event;

import protocol.Event;
import protocol.ObjectServer;
import protocol.visitor.VisitorServer;

public class DetachUserServerEv extends ObjectServer implements Event {
    String name;
    String detached = "detached.";
    public DetachUserServerEv(String date, String name){
        super(date);
        this.name = name;
    }
    public String getNickname() {
        return name;
    }

    public String getMessage(){
        return name+" "+detached;
    }

    public void accept(VisitorServer visitorServer) {
        visitorServer.visitDetachClient(this);
    }
}
