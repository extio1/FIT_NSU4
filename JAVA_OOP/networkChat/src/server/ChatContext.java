package server.ui;

import server.Server;
import server.logger.LoggerServer;

import javax.swing.*;
import java.awt.event.*;

public class ServerGUI extends JFrame{
    private final Server server;
    private JPanel mainFrame;
    private JTextArea textArea;
    private final LoggerServer logger;
    private final Refresher refresher = new Refresher();

    public ServerGUI(Server server){
        setSize(500, 500);
        setContentPane(mainFrame);
        setName("Chat server");

        refresher.setName("Server refresher thread");
        this.logger = server.getLogger();
        this.server = server;
        refresher.start();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                refresher.interrupt();
                server.shutdown();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        setFocusable(true);
        setVisible(true);
    }

    private class Refresher extends Thread{
        @Override
        public void run(){
            while(!Thread.interrupted()){
                String st;
                synchronized (logger) {
                    while(!logger.isNewStringAvailable() && !Thread.interrupted()) {
                        try {
                                logger.wait();
                        } catch (InterruptedException e) {
                            this.interrupt();
                            break;
                        }
                    }
                st = logger.getLastEventLog();
                }
                textArea.append(st);
                textArea.append("\n----------------------------------------------------\n");
            }
        }
    }
}

