package client.ui;

import client.Client;
import client.ClientSessionData;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.io.IOException;

public class ChatClientGui extends JFrame{
    private JTextArea userInputTextField;
    private JLabel userList;
    private JButton button1;
    private JPanel mainFrame;
    private JTextArea chatField;
    private JTextArea waitField;
    private JTextArea errorField;
    private JTextArea userListField;
    private JButton button2;
    private JLabel nameLabel;
    private final ClientSessionData data;
    private final Refresher refresher = new Refresher();
    private final Client client;

    public ChatClientGui(Client client){
        setSize(500, 500);
        setName("Chat client");

        this.client = client;

        register();

        setContentPane(mainFrame);

        data = client.getSessionData();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                try {
                    refresher.interrupt();
                    client.closeSession();
                } catch (InterruptedException | IOException ex) {
                    throw new RuntimeException(ex);
                }
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

        button2.setIcon(new ImageIcon("resources/images/btn_refresh.png"));
        button2.setOpaque(false);
        button2.setContentAreaFilled(false);

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.requestUserList();
                } catch (InterruptedException ignored){}
            }
        });

        button1.setIcon(new ImageIcon("resources/images/btn_send.png"));
        button1.setOpaque(false);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.send(userInputTextField.getDocument().getText(0, userInputTextField.getDocument().getLength()));
                    userInputTextField.setText("");
                } catch (BadLocationException | InterruptedException ignored){}
            }
        });

        setFocusable(true);
        setVisible(true);

        chatField.setEnabled(false);
        refresher.start();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void register(){
        String username = JOptionPane.showInputDialog("Enter your name (3-20 letters):",
                "Steve");
        try {
            client.registerNewUser(username);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class Refresher extends Thread{
        @Override
        public void run(){
            this.setName("GUI refresher thread");
            while (!Thread.interrupted()) {
                synchronized (data) {
                    refresh();
                    try {
                        data.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        private void refresh() {
            var messList = data.getChatHistory();
            chatField.setText("");
            for (String[] message : messList) {
                chatField.append(message[0] + "<" + message[1] + " says>: " + message[2] + "\n");
            }

            var userList = data.getUserSet();
            userListField.setText("");
            for (String user : userList) {
                userListField.append(user + '\n');
            }

            var errorList = data.getErrorList();
            errorField.setText("");
            for (String[] error : errorList) {
                errorField.append("\n" + error[0] + " : " + error[1] + "\n");
            }

            var waitingList = data.getWaitingRequestsList();
            waitField.setText("");
            for (String wait : waitingList) {
                waitField.append(wait + '\n');
            }

            if (data.isAskRequest()) {
                int ans = JOptionPane.showConfirmDialog(ChatClientGui.this, data.getAskMess(),
                        "User opinion dialog", JOptionPane.YES_NO_OPTION);
                data.setLastDialogReact(ans == 0);
            }

            nameLabel.setText(data.getNickname());
        }
    }
}
