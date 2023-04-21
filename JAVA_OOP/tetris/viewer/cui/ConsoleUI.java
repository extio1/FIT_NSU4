package viewer.cui;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import controller.CommandTetris;
import controller.Controller;
import model.Package;
import model.exception.UnattachedObserverException;
import model.figure.FigureDescriptor;
import observation.Subject;
import viewer.View;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class ConsoleUI extends View {
    private static final int FIELD_X = 10;
    private static final int FIELD_Y = 20;
    private static final int BEGIN_FIELD_X = 1;
    private static final int BEGIN_FIELD_Y = 1;

    private final boolean running = true;
    private final Screen screen;
    private final Controller controller;
    private final Subject subject;

    private volatile boolean suspendKeyLister = true;

    public ConsoleUI(Controller controller, Subject subject){
        this.controller = controller;
        this.subject = subject;
        screen = TerminalFacade.createScreen();
        screen.startScreen();
        keyListener.start();

        while(keyListener.getState() != State.WAITING)
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored){}

        this.start();
    }


    final Object syncObj = new Object();

    Thread keyListener = new Thread(new Runnable() {
        @Override
        public void run() {
            keyListener.setPriority(1);
            Key key;
            do {
                if (suspendKeyLister) {
                    synchronized (syncObj) {
                        try {
                            syncObj.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                key = screen.readInput();
                while (key == null) {
                    key = screen.readInput();
                }

                switch (key.getKind()) {
                    case ArrowLeft -> controller.execute(CommandTetris.Left);
                    case ArrowRight -> controller.execute(CommandTetris.Right);
                    case ArrowUp -> controller.execute(CommandTetris.Up);
                    case ArrowDown -> controller.execute(CommandTetris.Down);
                    case Escape -> {
                        controller.execute(CommandTetris.Pause);
                        switchConsoleToMenuMode();
                    }
                    case End -> {
                        exit();
                    }
                }
            } while (!Thread.interrupted() && running);
        }
    });

    @Override
    public void run() {
        while (!Thread.interrupted() && running) {
            synchronized (this) {
                try {
                    System.out.println("DOWN sleep");
                    this.wait();
                    System.out.println("UI waked up");
                } catch (InterruptedException e) {
                    break;
                }
            }

            Package pkg;
            try {
                pkg = (Package) subject.getInfo(this);
            } catch (UnattachedObserverException e) {
                throw new RuntimeException(e);
            }

            System.out.println(pkg.getState());

            switch(pkg.getState()){
                case defaultState -> refreshField(pkg);
                case onlyAboutState -> switchToAboutMode();
                case onlyScoresState -> switchToHighScores(pkg);
                case authState -> switchToAuthMode(pkg);
            }

        }
    }

    private final Terminal.Color[] colors = Terminal.Color.values();

    private void refreshField(Package pkg){
        synchronized (screen) {
            screen.clear();
            int[] gameField = pkg.getGameField();

            FigureDescriptor fdNow = pkg.getFallingFigureDescriptor();
            FigureDescriptor fdNext = pkg.getNextFigureDescriptor();

            drawField(gameField);
            drawInfo(pkg);
            drawFallingFigure(fdNow, pkg);
            drawNextFigure(fdNext, pkg);
            drawFrame();

            screen.refresh();
        }
    }

    private void switchToHighScores(Package pkg){
        suspendKeyLister = true;
        Key key;

        synchronized (screen) {
            screen.clear();
            screen.refresh();

            Map<String, Integer> stat = pkg.getPlayersStatistics();

            Set<Map.Entry<String, Integer>> records = stat.entrySet();
            Iterator<Map.Entry<String, Integer>> iRecords = records.iterator();
            for (int i = 0; i < Package.POSITIONS_IN_STAT; ++i) {
                if (iRecords.hasNext()) {
                    Map.Entry<String, Integer> record = iRecords.next();
                    screen.putString(1, i + 1, record.getKey() + "---" + record.getValue() + "\n",
                            Terminal.Color.WHITE, Terminal.Color.MAGENTA);
                } else {
                    break;
                }
            }
            screen.refresh();
        }
        while (true) {
            key = screen.readInput();
            while (key == null) {
                key = screen.readInput();
            }

            if (key.getKind() == Key.Kind.Escape) {
                controller.execute(CommandTetris.Resume);
                wakeUpKeyListener();
                break;
            }
        }
    }

    private void switchToAboutMode(){
        suspendKeyLister = true;
        Key key;

        synchronized (screen) {
            screen.clear();
            screen.putString(0, 1, "NSU - FIT - 21204 - SHALNEV TIMOFEY - 2023\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.putString(1, 3, "Up    arrow - rotate figure\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.putString(1, 4, "Left  arrow - move figure left\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.putString(1, 5, "Right arrow - move figure right\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.putString(1, 6, "Down  arrow - make figure faster\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.putString(0, 8, "Press ESC to resume game\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.refresh();
        }

        while(true) {
            key = screen.readInput();
            while (key == null) {
                key = screen.readInput();
            }

            if (key.getKind() == Key.Kind.Escape) {
                controller.execute(CommandTetris.Resume);
                wakeUpKeyListener();
                break;
            }
        }
    }

    private void switchConsoleToMenuMode(){
        suspendKeyLister = true;
        Key key;
        boolean live = true;

        synchronized (screen) {
            screen.clear();
            screen.putString(0, 0, "Welcome to menu! Press key to:\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.putString(5, 1, "1 - new game\n", Terminal.Color.WHITE, Terminal.Color.GREEN);
            screen.putString(5, 2, "2 - high scores\n", Terminal.Color.WHITE, Terminal.Color.GREEN);
            screen.putString(5, 3, "3 - about\n", Terminal.Color.WHITE, Terminal.Color.GREEN);
            screen.putString(5, 5, "Esc to resume the game.\n", Terminal.Color.WHITE, Terminal.Color.GREEN);
            screen.refresh();
        }

        while(live){
            key = screen.readInput();
            while(key == null){
                key = screen.readInput();
            }

            switch (key.getKind()){
                case Escape -> {
                    live = false;
                    controller.execute(CommandTetris.Resume);
                    wakeUpKeyListener();
                }
                case NormalKey -> {
                    char letter = key.getCharacter();

                    if(letter == '1'){
                        controller.execute(CommandTetris.NewGamePrepare);
                        wakeUpKeyListener();
                        live = false;
                        controller.execute(CommandTetris.Resume);
                    } else if (letter == '2') {
                        controller.execute(CommandTetris.HighScores);
                        wakeUpKeyListener();
                        live = false;
                    } else if (letter == '3'){
                        controller.execute(CommandTetris.About);
                        live = false;
                    }
                }
            }
        }
    }

    private void wakeUpKeyListener(){
        synchronized (syncObj) {
            suspendKeyLister = false;
            syncObj.notifyAll();
        }
    }

    private void switchToAuthMode(Package pkg){
        synchronized (syncObj) {
            suspendKeyLister = true;
            StringBuilder builder = new StringBuilder();
            Key key;
            boolean live = true;
            screen.putString(10, 0, "Enter your name:\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
            screen.refresh();

            while (live) {
                key = screen.readInput();
                while (key == null) {
                    key = screen.readInput();
                }
                switch (key.getKind()) {
                    case Enter -> live = false;
                    case Backspace -> {
                        if (builder.length() > 1)
                            builder.deleteCharAt(builder.length() - 1);
                        screen.clear();
                        screen.putString(10, 0, "Enter your name:\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
                        screen.putString(2, 2, builder.toString(), Terminal.Color.WHITE, Terminal.Color.MAGENTA);
                        screen.refresh();
                    }
                    case NormalKey -> {
                        char letter = key.getCharacter();
                        builder.append(letter);
                        screen.clear();
                        screen.putString(10, 0, "Enter your name:\n", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
                        screen.putString(2, 2, builder.toString(), Terminal.Color.WHITE, Terminal.Color.MAGENTA);
                        screen.refresh();
                    }
                    case End -> {
                        exit();
                    }
                }
            }

            String name = builder.toString();
            Package.setNickname(name);
            pkg.getPlayersStatistics().merge(Package.getNickname(), 0, Integer::max);

            controller.execute(CommandTetris.Resume);
            suspendKeyLister = false;
            syncObj.notifyAll();
            screen.clear();
            screen.refresh();
        }
    }

    private void drawInfo(Package pkg){
        screen.putString(15, 7, "Score: ", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
        screen.putString(15, 8, Integer.toString(pkg.getScore()), Terminal.Color.WHITE, Terminal.Color.MAGENTA);

        screen.putString(15, 10, "Speed: ", Terminal.Color.WHITE, Terminal.Color.MAGENTA);
        screen.putString(15, 11, Double.toString(pkg.getSpeed()), Terminal.Color.WHITE, Terminal.Color.MAGENTA);
    }

    private void drawFallingFigure(FigureDescriptor fd, Package pkg){
        for(int i = 0; i < fd.getLength(); ++i){
            for(int j = 0; j < fd.getWidth(); ++j){
                if(fd.getCurrentView()[i*fd.getWidth()+j] == 1){
                    screen.putString((fd.getPosX()+j)+1, (fd.getPosY()+i)+1,
                            "+", Terminal.Color.WHITE, colors[pkg.getCurrColor() % colors.length]);
                }
            }
        }
    }

    private void drawNextFigure(FigureDescriptor fd, Package pkg){
        for(int i = 0; i < fd.getInitCondLen(); ++i){
            for(int j = 0; j < fd.getInitCondWidth(); ++j){
                if(fd.getAllPossibleView()[0][i*fd.getInitCondWidth()+j] == 1){
                    screen.putString(15+j, 2+i,
                            "+", Terminal.Color.WHITE, colors[pkg.getNextColor() % colors.length]);
                }
            }
        }
    }

    private void drawField(int[] gameField){
        for(int i = 0; i < FIELD_Y; ++i){
            for(int j = 0; j < FIELD_X; ++j){
                int val = gameField[i * FIELD_X + j];
                if(val != 0){
                    screen.putString(j+1, i+1, "+", Terminal.Color.WHITE, colors[val % colors.length]);
                } else {
                    screen.putString(j+1, i+1, ".", Terminal.Color.WHITE, Terminal.Color.CYAN);
                }
            }
        }
    }

    private static int framePusher = 0;
    private void drawFrame(){
        for(int i = 0; i < 25; ++i){
            screen.putString(i, 0, "-", colors[(i+framePusher) % colors.length], Terminal.Color.BLACK);
            screen.putString(i, 22, "-", colors[(i+framePusher) % colors.length], Terminal.Color.BLACK);
        }
        for(int i = 0; i < 21; ++i){
            screen.putString(0, i, "|", colors[(i+framePusher) % colors.length], Terminal.Color.BLACK);
            screen.putString(25, i, "|", colors[(i+framePusher) % colors.length], Terminal.Color.BLACK);
        }
        screen.putString(0, 0, "*", colors[(framePusher) % colors.length], Terminal.Color.BLACK);
        screen.putString(25, 0, "*", colors[(framePusher) % colors.length], Terminal.Color.BLACK);
        screen.putString(0, 22, "*", colors[(framePusher) % colors.length], Terminal.Color.BLACK);
        screen.putString(25, 22, "*", colors[(framePusher) % colors.length], Terminal.Color.BLACK);
        ++framePusher;
    }

    private void exit(){
        controller.execute(CommandTetris.Exit);
        screen.stopScreen();
        this.interrupt();
        keyListener.interrupt();
    }

    @Override
    public void update() {
        synchronized (this) {
            System.out.println("TRYING TO UPDATE");
            this.notifyAll();
        }
    }
}
