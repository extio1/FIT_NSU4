import client.Client;
import client.clientImpls.clientSerialize.ClientSerialize;
import client.exception.ConfigurationException;
import client.view.ChatClientGui;
import server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = null;
        Client client = null;

        try {
            server = new Server();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        try {
            client = new ClientSerialize();
            ChatClientGui gui = new ChatClientGui(client);
        } catch (IOException | ConfigurationException e) {
            System.out.println(e.getMessage());
        }

    }
}