package view.gui;

import controller.CommandTetris;
import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightBank extends JPanel {
    private final Controller controller;

    RightBank(JLayeredPane jp, Controller controller){
        this.controller = controller;
        //setLayout(null);
        setBounds(10, 0,100,800);
        createMenuButton(jp);
    }

    private void createMenuButton(JLayeredPane jp){
        JButton menuButton = new JButton(new ImageIcon("resources/images/menu_button.png"));

        menuButton.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.execute(CommandTetris.Menu);
                System.out.println("Menu clicked");
                menuButton.setFocusable(false);
            }
        });

        add(menuButton);
    }
}
