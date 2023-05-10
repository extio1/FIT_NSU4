package client.clientImpls;

import client.Client;
import client.exception.ConfigurationException;
import client.exception.UnknownImplementationException;
import protocol.userObject.UserMessageFabric;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Properties;

public class ClientSerialize implements Client {
    private final ServerListener serverOutput;
    private final ObjectOutputStream clientOutput;

    private final Socket socket;
    private final ClientProperty configuration;

    private final UserMessageFabric messageFabric;

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

        serverOutput = new ServerListener(socket.getInputStream());
        clientOutput = new ObjectOutputStream(socket.getOutputStream());

        serverOutput.start();
    }

    @Override
    public void send(String message) throws IOException {
        clientOutput.writeObject(messageFabric.makeMessageUser(message));
    }

    @Override
    public void closeSession() throws IOException {
        clientOutput.writeObject(messageFabric.makeDetachUser());
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
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private ClientProperty setupByConfig(String path){
        Properties properties = new Properties();
        String ip = properties.getProperty("serverName");
        int port = Integer.parseInt(properties.getProperty("port"));
        String implName = properties.getProperty("implementation");

        return new ClientProperty(ip, port, implName);
    }

    private record ClientProperty(
            String hostName,
            int port,
            String implName
    ) {}
}
