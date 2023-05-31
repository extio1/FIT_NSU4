package client;

import exception.ConfigurationException;
import client.reciever.ReceiverClient;
import client.sender.SenderClient;
import exception.UnknownImplementationException;
import protocol.Request;
import protocol.clientObject.UserObjFabric;
import protocol.visitor.VisitorServer;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private ReceiverClient receiver;
    private SenderClient sender;
    private Ping ping;
    private final UserObjFabric messageFabric = new UserObjFabric();
    private final ClientSessionData sessionData = new ClientSessionData();
    private final BlockingQueue<Request> toSendQueue = new LinkedBlockingQueue<>();
    private Socket socket;
    private final ClientProperty configuration;
    VisitorServer visitorServerMessages = new VisitorServer(sessionData);

    public void requestUserList() throws InterruptedException {
        toSendQueue.put(messageFabric.makeListUserRequest());
    }
    public void send(String message) throws InterruptedException {
        toSendQueue.put(messageFabric.makeMessageUserRequest(message));
    }
    public void registerNewUser(String user) throws InterruptedException {
        toSendQueue.put(messageFabric.makeRegisterUserRequest(user));
    }

    public ClientSessionData getSessionData() {
        return sessionData;
    }
    public void closeSession() throws InterruptedException, IOException {
        toSendQueue.put(messageFabric.makeDetachUserRequest(false));

        terminateConnection();
    }

    private void terminateConnection() throws InterruptedException, IOException {
        ping.interrupt();
        sender.interrupt();
        receiver.interrupt();

        sender.join();
        socket.close();
    }

    public Client() throws IOException, ConfigurationException, InterruptedException {
        this("resources/client.properties");
    }

    public Client(String configPath) throws IOException, ConfigurationException, InterruptedException {
        configuration = setupByConfig(configPath);

        ping = new Ping();
        socket = new Socket(configuration.hostName, configuration.port);

        sender = SenderClient.makeSender(toSendQueue, sessionData, socket, configuration.impl);
        receiver = ReceiverClient.makeReceiver(visitorServerMessages, socket, configuration.impl);

        receiver.start();
        sender.start();
        ping.start();

        System.out.println("Client launched at "+ socket.getInetAddress()+" port "+ socket.getPort());
    }

    private record ClientProperty(
            String hostName,
            int port,
            ImplementationClient impl,
            int timeout
    ) {}

    private ClientProperty setupByConfig(String path) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));

        String ip = properties.getProperty("serverName");
        int port = Integer.parseInt(properties.getProperty("port"));
        ImplementationClient implName = ImplementationClient.valueOf(properties.getProperty("implementation"));
        int timeout = Integer.parseInt(properties.getProperty("timeout"));

        return new ClientProperty(ip, port, implName, timeout);
    }

    private void reconnect() throws IOException, UnknownImplementationException, InterruptedException {
        try {
            //if wasn't IOException
            sender.interrupt();
            receiver.interrupt();
            socket.close();

            toSendQueue.clear();

            socket = new Socket(configuration.hostName, configuration.port);

            ping = new Ping();
            sender = SenderClient.makeSender(toSendQueue, sessionData, socket, configuration.impl);
            receiver = ReceiverClient.makeReceiver(visitorServerMessages, socket, configuration.impl);

            receiver.start();
            sender.start();
            ping.start();

            registerNewUser(sessionData.getNickname());

            System.out.println("Client reconnected at " + socket.getInetAddress() + " port " + socket.getPort());
        } catch (IOException e){
            System.out.println("Server is unreachable");
        }
    }

    private class Ping extends Thread{
        private static final int ECHO_PORT = 7;
        private static final int PERIOD = 1000;

        private final Socket srv;

        public Ping() throws IOException {
            this.setName("Ping: client");

            srv = new Socket();
            srv.connect(new InetSocketAddress("localhost", ECHO_PORT));
            srv.setSoTimeout(configuration.timeout);
        }
        private InputStream in;
        private OutputStream out;
        @Override
        public void run() {
            try {
                in = srv.getInputStream();
                out = srv.getOutputStream();
                while (!Thread.interrupted()) {
                    try {
                        doPing();
                        sleep(PERIOD);
                    } catch (SocketException e) {
                        boolean agree = sessionData.askYesNo("Server refused connection\nReconnect?");
                        if(agree) {
                            reconnect();
                        } else {
                            terminateConnection();
                        }
                        break;
                    } catch (SocketTimeoutException e){
                        boolean agree = sessionData.askYesNo("Server didn't response for " + configuration.timeout/1000 + "sec." +
                                "\nWait?");
                        if(!agree){
                            terminateConnection();
                            break;
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException | UnknownImplementationException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) {}
        }

        public void doPing() throws IOException {
            out.write(1);
            int rd = in.read();
        }
    }
}
