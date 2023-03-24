package controller;

import tetrisModel.Tetris;

import static controller.CommandTetris.Left;
import static controller.CommandTetris.Right;

public class TetrisController implements Controller{
    Tetris myModel = null;

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
            case NewGame -> myModel.newGame();
            case HighScores -> myModel.highScores();
            case About -> myModel.about();
        }
    }

}
