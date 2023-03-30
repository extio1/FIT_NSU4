package view.gui;

import controller.CommandTetris;
import controller.Controller;
import tetrisModel.Package;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {
    private final Controller controller;
    private final GraphicUI myGui;
    private final JLayeredPane lp = getLayeredPane();

    private final RightBank rb;
    private final GameField gf;

    public void updateWindow(Package pkg){
        rb.updateNextFigure(pkg.getFigureView(), pkg.getFigureWidth(), pkg.getFigureLength(), pkg.getFigureNumber());
        gf.updateFallingFigure(pkg.getFigureView(), pkg.getFigureWidth(), pkg.getFigureLength(),
                               pkg.getFigurePosX(), pkg.getFigurePosY(), pkg.getFigureNumber());
        gf.updateField(pkg.getGameField());
    }

    MainWindow(int xSize, int ySize, Controller controller, GraphicUI myGui) throws InterruptedException {
        super("Tetris");
        this.myGui = myGui;
        this.controller = controller;

        rb = new RightBank(controller, lp);
        gf = new GameField();

        setBounds(100, 100, 610, 800);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon("resources/images/main_frame_icon.jpg").getImage());

        lp.add(rb, JLayeredPane.PALETTE_LAYER);
        lp.add(gf, JLayeredPane.PALETTE_LAYER);

        add(gf);
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
                    controller.execute(CommandTetris.Exit);
                    myGui.killMe();
                    dispose();
                }
            }
        });

        setVisible(true);
        setFocusable(true);
    }

}
