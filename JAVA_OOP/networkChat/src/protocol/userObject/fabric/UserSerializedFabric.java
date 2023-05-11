package protocol.userObject.fabric;

import protocol.userObject.*;

public class UserSerializedFabric implements UserMessageFabric {
    @Override
    public DetachUser makeDetachUser() {
        return null;
    }

    @Override
    public ListUser makeListUser() {
        return null;
    }

    @Override
    public MessageUser makeMessageUser(String message) {
        return null;
    }

    @Override
    public RegisterUser makeRegisterUser(String name) {
        return null;
    }
}
