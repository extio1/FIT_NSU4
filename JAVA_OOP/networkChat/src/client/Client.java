package client;

import java.io.IOException;

public interface Client {
    void send(String message) throws IOException;
    void closeSession() throws IOException;
    void registerNewUser(String name) throws IOException;
    void requestUserList() throws IOException;
}
