package protocol.userObject.fabric;

import protocol.userObject.*;
import protocol.userObject.serialization.DetachUserSerialized;
import protocol.userObject.serialization.ListUserSerialized;
import protocol.userObject.serialization.MessageUserSerialized;
import protocol.userObject.serialization.RegisterUserSerialized;

public class UserSerializedFabric implements UserMessageFabric {
    @Override
    public DetachUser makeDetachUser() {
        return new DetachUserSerialized();
    }

    @Override
    public ListUser makeListUser() {
        return new ListUserSerialized();
    }

    @Override
    public MessageUser makeMessageUser(String message) {
        return new MessageUserSerialized(message);
    }

    @Override
    public RegisterUser makeRegisterUser(String name) {
        return new RegisterUserSerialized(name);
    }
}
