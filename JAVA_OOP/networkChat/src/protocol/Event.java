package protocol;

import protocol.serverObject.VisitorServer;

import java.io.Serializable;

public interface Event extends Serializable {
    String getDate();
    void accept(VisitorServer visitorServer);
}
