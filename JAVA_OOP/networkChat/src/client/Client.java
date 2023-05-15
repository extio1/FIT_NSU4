package client;

import java.io.IOException;

public interface Client {
    void send(String message) throws IOException, InterruptedException;
    void closeSession() throws IOException, InterruptedException;
    void registerNewUser(String name) throws IOException, InterruptedException;
    void requestUserList() throws IOException, InterruptedException;
}
