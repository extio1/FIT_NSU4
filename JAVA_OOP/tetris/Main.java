import view.GUI.GraphicUI;
import controller.CommandTetris;
import controller.Controller;
import controller.TetrisController;
import tetrisModel.Tetris;

import javax.swing.*;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Tetris game = new Tetris();
        Controller controller = new TetrisController(game);

        GraphicUI ui = new GraphicUI(800, 600, controller, game);
        ui.setVisibleMainFrame(true);

        game.attach(ui);

        game.turnOn();

        controller.execute(CommandTetris.Left);
        sleep(60000);
        controller.execute(CommandTetris.Exit);

    }
}
