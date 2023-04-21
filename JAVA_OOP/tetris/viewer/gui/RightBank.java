package viewer.gui;

import controller.CommandTetris;
import controller.Controller;
import model.Package;
import model.figure.FigureDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

class RightBank extends JPanel {
    private final static int X_SIZE_NEXT_FIGURE_FIELD = 3;
    private final static int Y_SIZE_NEXT_FIGURE_FIELD = 4;
    private final static int CELL_SIZE = 50;

    private final MainWindow myFrame;
    private final Controller controller;
    private byte[] nextFigureView = null;
    private int nextFigureXSize;
    private int nextFigureYSize;
    private int figureColor;
    private int score;
    private float speed;
    private Package pkg = null;

    private final JTextField scoreText = new JTextField();
    private final JTextField gameSpeedText = new JTextField();
    JDialog aboutDialog = new JDialog();

    RightBank(Controller controller, JLayeredPane lp, MainWindow frame){
        this.controller = controller;
        JTextArea scoreT = new JTextArea("Score:");
        scoreT.setBounds(450, 280, 100, 20);
        JTextArea speedT = new JTextArea("Speed:");
        speedT.setBounds(450, 330, 100, 20);
        myFrame = frame;

        speedT.setBorder(null);
        scoreT.setBorder(null);
        scoreT.setEditable(false);
        scoreT.setCursor(null);
        scoreT.setOpaque(false);
        scoreT.setFocusable(false);
        speedT.setEditable(false);
        speedT.setCursor(null);
        speedT.setOpaque(false);
        speedT.setFocusable(false);

        scoreText.setBounds(450, 290, 100, 20);
        gameSpeedText.setBounds(450, 340, 100, 20);
        scoreText.setOpaque(false);
        gameSpeedText.setOpaque(false);
        scoreT.setOpaque(false);
        speedT.setOpaque(false);

        setBounds(440,0,200,870);
        setLayout(null);
        setOpaque(true);

        createAboutButton();
        createHighScoresButton();
        createAboutButton();
        createNewGameButton();

        aboutDialog.setLayout(new FlowLayout());
        aboutDialog.setBounds(400, 350, 500, 400);

        aboutDialog = new JDialog(frame);
        JLabel label = new JLabel("NSU - FIT - 21204 - SHALNEV TIMOFEY - 2023");
        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.execute(CommandTetris.Resume);
                aboutDialog.setFocusable(false);
                aboutDialog.setVisible(false);
            }
        });

        aboutDialog.add(label);
        aboutDialog.add(button);

        scoreText.setBorder(null);
        gameSpeedText.setBorder(null);

        add(scoreT);
        add(speedT);
        add(gameSpeedText);
        add(scoreText);
        lp.add(gameSpeedText, 2);
        lp.add(scoreText, 2);
    }

    public void setPkg(Package p){
        pkg = p;
    }

    public void updateTextSection(int score, float speed){
        this.score = score;
        this.speed = speed;
    }

    public void updateNextFigure(FigureDescriptor fd, int color){
        nextFigureView  = fd.getAllPossibleView()[0];
        nextFigureXSize = fd.getInitCondWidth();
        nextFigureYSize = fd.getInitCondLen();
        figureColor       = color;
        repaint();
    }

    @Override
    public void paintComponent(Graphics _g){
        Graphics2D g = (Graphics2D) _g;

        g.setColor(new Color(52, 77, 79, 255));
        g.fillRect(400, 0, 200, 870); // right hood

        g.setColor(new Color(50, 94, 98, 255));
        g.fillRect(415, 20, 170, 350); // figure place

        g.setColor(new Color(88, 120, 122, 255));
        g.fillRect(435, 265, 128, 100); // info text place

        g.setColor(new Color(figureColor*10%256,figureColor*5%256,figureColor*2%256));
        for(int i = 0; i < Y_SIZE_NEXT_FIGURE_FIELD; ++i){
            if(i < nextFigureYSize) {
                for (int j = 0; j < X_SIZE_NEXT_FIGURE_FIELD; ++j) {
                    if(j < nextFigureXSize && nextFigureView[i*nextFigureXSize+j] == 1){
                        g.fillRect(425+CELL_SIZE*j, 30+CELL_SIZE*i, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
        drawGrid(g);
        scoreText.setText(""+score);
        gameSpeedText.setText(""+speed+"x");
    }

    private void createNewGameButton(){
        JButton ngButton = new JButton("New game");
        ngButton.setBackground(Color.WHITE);

        ngButton.setBounds(420,450,156,64);

        ngButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.execute(CommandTetris.NewGamePrepare);
                pkg.getPlayersStatistics().merge(Package.getNickname(), pkg.getScore(), Integer::max);
                ngButton.setFocusable(false);
                myFrame.getBackFocus();
            }
        });

        add(ngButton);
    }

    private void createHighScoresButton() {
        JButton hsButton = new JButton("High Scores");
        hsButton.setBackground(Color.WHITE);

        hsButton.setBounds(420,530,156,64);

        hsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.execute(CommandTetris.HighScores);
                hsButton.setFocusable(false);
            }
        });

        add(hsButton);
    }

    private void createAboutButton(){
        JButton abButton = new JButton("About");
        abButton.setBackground(Color.WHITE);

        abButton.setBounds(420,610,156,64);

        abButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.execute(CommandTetris.About);
                abButton.setFocusable(false);
            }
        });

        add(abButton);
    }

    private void drawGrid(Graphics g){
        int CELL_SIZE = 50;
        int gridSizeX = 4;
        int gridSizeY = 5;
        g.setColor(new Color(158, 182, 185, 255));
        for(int i = 0; i < gridSizeY; ++i){
            g.drawLine(425,30+CELL_SIZE*i,575, 30+CELL_SIZE*i);
        }
        for(int i = 0; i < gridSizeX; ++i){
            g.drawLine(425+CELL_SIZE*i,30,425+CELL_SIZE*i,230);
        }
    }

}
