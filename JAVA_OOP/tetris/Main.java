import controller.Controller;
import controller.TetrisController;

import model.Tetris;

import viewer.gui.GraphicUI;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        int WIDTH_SIZE_GAME_FIELD = 10;
        int LENGTH_SIZE_GAME_FIELD = 20;

        Tetris game = new Tetris();
        Controller controller = new TetrisController(game);
        GraphicUI ui = new GraphicUI(WIDTH_SIZE_GAME_FIELD, LENGTH_SIZE_GAME_FIELD, controller, game);
        game.attach(ui);

        game.turnOn();

    }
}
