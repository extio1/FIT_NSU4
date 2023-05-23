package protocol;


import client.ClientSessionData;
import protocol.userObject.VisitorClient;

import java.io.Serializable;

public interface Request extends Serializable {
    long getId();
    void accept(VisitorClient v);
    void handleServerResponse(Response r, ClientSessionData data);
}
