package view.gui;

import controller.CommandTetris;
import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {
    private final Controller controller;
    private final JLayeredPane lp = getLayeredPane();

    MainWindow(Controller controller){
        super("Tetris");
        this.controller = controller;
        RightBank rb = new RightBank(controller);

        setBounds(600, 100, 600, 800);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon("resources/images/main_frame_icon.jpg").getImage());

        //lp.add(rb, JLayeredPane.PALETTE_LAYER);
        add(rb);

        addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_LEFT) controller.execute(CommandTetris.Left);
                if (e.getKeyCode()==KeyEvent.VK_RIGHT) controller.execute(CommandTetris.Right);
                if (e.getKeyCode()==KeyEvent.VK_UP) controller.execute(CommandTetris.Up);
                if (e.getKeyCode()==KeyEvent.VK_DOWN) controller.execute(CommandTetris.Down);
                if (e.getKeyCode()==KeyEvent.VK_ESCAPE) controller.execute(CommandTetris.Menu);
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {}
            public void windowClosed(WindowEvent event) {}
            public void windowDeactivated(WindowEvent event) {}
            public void windowDeiconified(WindowEvent event) {}
            public void windowIconified(WindowEvent event) {}
            public void windowOpened(WindowEvent event) {}
            public void windowClosing(WindowEvent event) {
                Object[] options = { "Да", "Нет" };
                int rc = JOptionPane.showOptionDialog(
                        event.getWindow(), "Закрыть окно?",
                        "Подтверждение", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        new ImageIcon("resources/images/question_head.png"),
                        null, options);

                if (rc == 0) {
                    event.getWindow().setVisible(false);
                    System.exit(0);
                }
            }
        });

        setFocusable(true);
    }
}
