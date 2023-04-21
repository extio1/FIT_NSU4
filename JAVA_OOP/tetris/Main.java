package main;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import controller.CommandTetris;
import controller.Controller;
import controller.TetrisController;

import model.Tetris;
import com.googlecode.lanterna.*;

import viewer.View;
import viewer.cui.ConsoleUI;
import viewer.gui.GraphicUI;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Tetris game = new Tetris();
        Controller controller = new TetrisController(game);
        View ui = null;

        StartBy start = StartBy.CONSOLE;
        switch(start){
            case CONSOLE -> ui = new ConsoleUI(controller, game);
            case SWING -> ui = new GraphicUI(controller, game);
        }

        game.attach(ui);
        while (ui.getState() != Thread.State.WAITING || game.getState() != Thread.State.WAITING) {
            Thread.sleep(500);
        }

        controller.execute(CommandTetris.Launch);
    }


    private enum StartBy{
        CONSOLE,
        SWING
    }
}
