package controller;

public interface Controller<ModelT> {
    void execute(CommandTetris comm);
}
