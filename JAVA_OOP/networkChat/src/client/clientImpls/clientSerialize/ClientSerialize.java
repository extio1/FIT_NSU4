package client.clientImpls.clientSerialize;

import client.Client;
import client.exception.ConfigurationException;
import client.exception.UnknownImplementationException;
import protocol.userObject.UserMessageFabric;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Properties;

public class ClientSerialize implements Client {
    private final ServerListener serverListener;
    private final ObjectOutputStream clientOutput;

    private final Socket socket;
    private final ClientProperty configuration;

    private final UserMessageFabric messageFabric;

    @Override
    public void requestUserList() throws IOException {
        clientOutput.writeObject(messageFabric.makeListUser());
    }

    @Override
    public void send(String message) throws IOException {
        clientOutput.writeObject(messageFabric.makeMessageUser(message));
    }

    @Override
    public void closeSession() throws IOException {
        clientOutput.writeObject(messageFabric.makeDetachUser());
    }

    @Override
    public void registerNewUser(String user) throws IOException {
        clientOutput.writeObject(messageFabric.makeRegisterUser(user));
    }

    public ClientSerialize() throws IOException, ConfigurationException {
        this("resources/client.properties");
    }

    public ClientSerialize(String configPath) throws IOException, ConfigurationException {
        configuration = setupByConfig(configPath);
        socket = new Socket(configuration.hostName, configuration.port);
        try {
            messageFabric = (UserMessageFabric) Class.forName(configuration.implName).getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e){
            throw new UnknownImplementationException(configuration.implName);
        }

        serverListener = new ServerListener(socket);
        clientOutput = new ObjectOutputStream(socket.getOutputStream());

        serverListener.start();
    }

    private static class ServerListener extends Thread{
        ObjectInputStream in;
        public ServerListener(Socket socketToListen) throws IOException {
            this.in = new ObjectInputStream(socketToListen.getInputStream());
        }

        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    Object obj = in.readObject();
                    System.out.println();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private record ClientProperty(
            String hostName,
            int port,
            String implName
    ) {}

    private ClientProperty setupByConfig(String path) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));

        String ip = properties.getProperty("serverName");
        int port = Integer.parseInt(properties.getProperty("port"));
        String implName = properties.getProperty("implementation");

        return new ClientProperty(ip, port, implName);
    }
}
