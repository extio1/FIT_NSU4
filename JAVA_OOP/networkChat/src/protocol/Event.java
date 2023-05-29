package protocol;


import protocol.visitor.VisitorServer;

import java.io.Serializable;

public interface Event extends Serializable {
    String getDate();
    void accept(VisitorServer visitorServer);
}
