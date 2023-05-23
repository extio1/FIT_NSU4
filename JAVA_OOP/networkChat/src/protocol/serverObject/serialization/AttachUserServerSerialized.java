package protocol.serverObject.serialization;

import protocol.ObjectServer;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.event.AttachUserServer;

public class AttachUserServerSerialized extends ObjectServer implements AttachUserServer {
    private final String name;
    public AttachUserServerSerialized(String date, String name){
        super(date);
        this.name = name;
    }
    @Override
    public String getNickname() {
        return name;
    }
    @Override
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitAttachClient(this);
    }
}
