package protocol.userObject;

public interface UserMessageFabric {
    DetachUser makeDetachUser();
    ListUser makeListUser();
    MessageUser makeMessageUser(String message);
    RegisterUser makeRegisterUser(String name);
}
