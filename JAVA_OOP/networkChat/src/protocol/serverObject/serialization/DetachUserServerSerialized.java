package protocol.serverObject.serialization;

import protocol.ObjectServer;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.event.DetachUserServer;

public class DetachUserServerSerialized extends ObjectServer implements DetachUserServer {
    String name;
    public DetachUserServerSerialized(String date, String name){
        super(date);
        this.name = name;
    }
    @Override
    public String getNickname() {
        return name;
    }

    @Override
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitDetachClient(this);
    }
}
