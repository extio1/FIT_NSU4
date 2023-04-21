package controller;

import model.Tetris;

public class TetrisController implements Controller{
    Tetris myModel;

    public TetrisController(Tetris _Tetris){
        myModel = _Tetris;
    }

    @Override
    public void execute(CommandTetris comm) {
        switch (comm) {
            case Left -> myModel.left();
            case Right -> myModel.right();
            case Up -> myModel.up();
            case Down -> myModel.down();
            case Exit -> myModel.exit();
            case Pause -> myModel.pauseGame();
            case NewGamePrepare -> myModel.newGame();
            case Launch -> myModel.launch();
            case HighScores -> myModel.highScores();
            case Resume -> myModel.continueGame();
            case About -> myModel.about();
        }
    }

}
