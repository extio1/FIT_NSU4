package protocol;

import protocol.ObjectServer;

public interface Request {
    void handleServerResponse(ObjectServer response);
}
