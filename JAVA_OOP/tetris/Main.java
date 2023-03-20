import controller.CommandTetris;
import controller.Controller;
import controller.TetrisController;
import tetrisModel.Tetris;

public class Main {
    public static void main(String[] args){
        Tetris game = new Tetris();
        Controller<Tetris> controller = new TetrisController(game);

        game.start();

        controller.execute(CommandTetris.Left);
        controller.execute(CommandTetris.Exit);
    }
}
