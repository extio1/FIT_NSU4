package protocol.userObject.serialization;

import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.userObject.RegisterUser;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class RegisterUserSerialized extends ObjectUser implements Serializable, RegisterUser {
    private final String data;

    public RegisterUserSerialized(String name){
        super();
        data = name;
    }

    public String getData() {
        return data;
    }

    @Override
    public void handleServerResponse(ObjectServer response) {

    }
}
