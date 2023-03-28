package view.gui;

import controller.CommandTetris;
import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightBank extends JComponent {
    private final Controller controller;

    RightBank(Controller controller){
        this.controller = controller;
        setBounds(150, 450, 150, 800);
        setLayout(null);
        createMenuButton();
    }

    @Override
    public void paintComponent(Graphics g){
        Graphics2D mainRect = (Graphics2D) g;
        mainRect.setColor(new Color(63, 76, 62, 255));
        mainRect.fillRect(450, 0, 180, 800);
    }

    private void createMenuButton(){
        JButton menuButton = new JButton(new ImageIcon("resources/images/menu_button.png"));
        menuButton.setRolloverIcon(new ImageIcon("resources/images/menu_button_pressed.png"));
        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBounds(490,690,64,64);

        menuButton.addMouseListener(new MouseAdapter() {
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
