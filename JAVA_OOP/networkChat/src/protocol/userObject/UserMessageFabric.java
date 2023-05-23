package protocol.userObject;

import protocol.userObject.request.DetachUser;
import protocol.userObject.request.ListUser;
import protocol.userObject.request.MessageUser;
import protocol.userObject.request.RegisterUser;

public interface UserMessageFabric {
    DetachUser makeDetachUserRequest(boolean isTimeout);
    ListUser makeListUserRequest();
    MessageUser makeMessageUserRequest(String message);
    RegisterUser makeRegisterUserRequest(String name);
}
