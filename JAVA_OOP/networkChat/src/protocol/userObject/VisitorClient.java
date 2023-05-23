package protocol.userObject;

import protocol.userObject.request.DetachUser;
import protocol.userObject.request.ListUser;
import protocol.userObject.request.MessageUser;
import protocol.userObject.request.RegisterUser;

public interface VisitorClient {
    void visitMessageRequest(MessageUser r);
    void visitListUserRequest(ListUser r);
    void visitRegisterUserRequest(RegisterUser r);
    void visitDetachUserRequest(DetachUser r);
}
