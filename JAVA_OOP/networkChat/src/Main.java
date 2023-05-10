import client.Client;
import client.clientImpls.ClientSerialize;
import client.view.ChatClientGui;
import server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        try {
            Client clientConsole = new ClientSerialize();
            ChatClientGui gui = new ChatClientGui(clientConsole);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}