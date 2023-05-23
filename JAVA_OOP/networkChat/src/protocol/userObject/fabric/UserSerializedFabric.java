package protocol.userObject.fabric;

import protocol.userObject.*;
import protocol.userObject.request.DetachUser;
import protocol.userObject.request.ListUser;
import protocol.userObject.request.MessageUser;
import protocol.userObject.request.RegisterUser;
import protocol.userObject.serialization.DetachUserSerialized;
import protocol.userObject.serialization.ListUserSerialized;
import protocol.userObject.serialization.MessageUserSerialized;
import protocol.userObject.serialization.RegisterUserSerialized;

public class UserSerializedFabric implements UserMessageFabric {
    @Override
    public DetachUser makeDetachUserRequest(boolean isTimeout) {
        return new DetachUserSerialized(isTimeout);
    }

    @Override
    public ListUser makeListUserRequest() {
        return new ListUserSerialized();
    }

    @Override
    public MessageUser makeMessageUserRequest(String message) {
        return new MessageUserSerialized(message);
    }

    @Override
    public RegisterUser makeRegisterUserRequest(String name) {
        return new RegisterUserSerialized(name);
    }
}
