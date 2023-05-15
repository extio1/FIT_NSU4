package client.view;

import client.Client;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

public class ChatClientGui extends JFrame{
    private JTextArea userInputTextField;
    private final Client client;

    public ChatClientGui(Client client){
        String username = JOptionPane.showInputDialog(this, "Enter your name:");

        this.client = client;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
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

        try {
            client.registerNewUser(username);
        } catch (IOException | InterruptedException e){
            System.out.println(e.getMessage());
        }

    }
}
