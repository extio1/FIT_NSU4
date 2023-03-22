package controller;

import tetrisModel.Tetris;

import static controller.CommandTetris.Left;
import static controller.CommandTetris.Right;

public class TetrisController implements Controller<Tetris>{
    Tetris myModel = null;

    public TetrisController(Tetris _Tetris){
        myModel = _Tetris;
    }

    @Override
    public void execute(CommandTetris comm) {
        switch (comm){
            case Left -> myModel.left();
            case Right -> myModel.right();
            case Exit -> myModel.exit();
        }
    }

}
