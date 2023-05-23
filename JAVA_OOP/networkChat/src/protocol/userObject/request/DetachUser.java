package protocol.userObject.request;

import protocol.Request;

public interface DetachUser extends Request {
    boolean isTimeout();
}
