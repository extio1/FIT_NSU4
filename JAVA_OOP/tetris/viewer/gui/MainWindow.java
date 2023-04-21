package viewer.gui;

import controller.CommandTetris;
import controller.Controller;
import controller.TetrisController;
import model.Package;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class MainWindow extends JFrame {
    private final Controller controller;
    private final GraphicUI myGui;
    private final JLayeredPane lp = getLayeredPane();

    private final RightBank rb;
    private final GameField gf;
    private Package pkgNow = null;

    public void updateWindow(Package pkg){
        rb.setPkg(pkg);
        pkgNow = pkg;

        rb.updateTextSection(pkg.getScore(), pkg.getSpeed());
        rb.updateNextFigure(pkg.getNextFigureDescriptor(), pkg.getNextColor());
        gf.updateFallingFigure(pkg.getFallingFigureDescriptor(), pkg.getCurrColor());
        gf.updateField(pkg.getGameField());
    }

    public void askAuth(Package pkg){
        controller.execute(CommandTetris.Pause);

        String name = (String) JOptionPane.showInputDialog(this, "Enter your name",
                "Name", JOptionPane.QUESTION_MESSAGE, new ImageIcon("/resources/images/question_head.png"),
                null, "Player");

        pkg.getPlayersStatistics().merge(name, 0, Integer::max);
        Package.setNickname(name);

        requestFocus(true);
        controller.execute(CommandTetris.Resume);
    }

    private static final int POSITIONS_IN_STAT = 5;
    public void showScores(Package pkg){
        controller.execute(CommandTetris.Pause);

        Map<String, Integer> stat = pkg.getPlayersStatistics();
        String[] msg = new String[POSITIONS_IN_STAT];

        Set<Map.Entry<String, Integer>> records = stat.entrySet();
        Iterator<Map.Entry<String, Integer>> iRecords = records.iterator();
        for(int i = 0; i < POSITIONS_IN_STAT; ++i){
            if(iRecords.hasNext()) {
                Map.Entry<String, Integer> record = iRecords.next();
                msg[i] = record.getKey()+" --- "+record.getValue();
            } else {
                break;
            }
        }
        JOptionPane.showMessageDialog(this,
                msg,
                "Game records",
                JOptionPane.INFORMATION_MESSAGE);

        requestFocus(true);
        controller.execute(CommandTetris.Resume);
    }

    public void getBackFocus(){
        this.requestFocus(true);
    }

    public void showAbout(){
        JOptionPane.showMessageDialog(this, "NSU - FIT - 21204 - SHALNEV TIMOFEY - 2023");
        controller.execute(CommandTetris.Resume);
        requestFocus(true);
    }

    MainWindow(Controller controller, GraphicUI myGui) {
        super("Tetris");
        this.myGui = myGui;
        this.controller = controller;

        gf = new GameField();
        rb = new RightBank(controller, lp, this);

        setBounds(100, 0, 610, 770);
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
                if (e.getKeyCode()==KeyEvent.VK_ESCAPE) controller.execute(CommandTetris.Resume);
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
                    if(pkgNow != null) {
                        pkgNow.getPlayersStatistics().merge(Package.getNickname(), pkgNow.getScore(), Integer::max);
                    }
                    controller.execute(CommandTetris.Exit);
                    myGui.interrupt();
                    dispose();
                }
            }
        });

        setVisible(true);
        setFocusable(true);
    }

}
