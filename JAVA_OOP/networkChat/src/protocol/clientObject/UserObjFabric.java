package protocol.clientObject;

import protocol.clientObject.request.DetachUserReq;
import protocol.clientObject.request.ListUserReq;
import protocol.clientObject.request.MessageUserReq;
import protocol.clientObject.request.RegisterUserReq;

public class UserObjFabric {
    
    public DetachUserReq makeDetachUserRequest(boolean isTimeout) {
        return new DetachUserReq(isTimeout);
    }

    
    public ListUserReq makeListUserRequest() {
        return new ListUserReq();
    }

    
    public MessageUserReq makeMessageUserRequest(String message) {
        return new MessageUserReq(message);
    }

    
    public RegisterUserReq makeRegisterUserRequest(String name) {
        return new RegisterUserReq(name);
    }
}
