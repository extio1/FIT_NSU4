package client.clientImpls;

import client.Client;

import java.io.*;
import java.net.Socket;

public class ClientSerialize implements Client {
    private UserInput userInput;
    private ServerListener serverInput;

    private Socket socket;

    public ClientSerialize(String host, int port) throws IOException {
        socket = new Socket(host, port);

        serverInput = new ServerListener(socket.getInputStream());
        userInput.start();
        serverInput.start();
    }

    @Override
    public void send() {

    }

    @Override
    public void closeSession() {

    }


    private static class UserInput extends Thread{
        private BufferedInputStream userMessageReader;
        public void connectMassageWriter(BufferedInputStream userInputStream) throws IOException {
            this.userMessageReader = userInputStream;
        }

        @Override
        public void run() {
            while(!Thread.interrupted()){
            }
        }
    }

    private static class ServerListener extends Thread{
        ObjectInputStream in;
        public ServerListener(InputStream in) throws IOException {
            this.in = new ObjectInputStream(in);
        }

        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    Object obj = in.readObject();
                    System.out.println();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(e.getMessage());;
                }
            }
        }
    }

}
