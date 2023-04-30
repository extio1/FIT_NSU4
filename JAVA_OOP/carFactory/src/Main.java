import controller.FabricController;
import factory.Factory;
import gui.FactoryGUI;

/*
    Рубрика вопросы.

    ВОПРОС: Что произойдёт, если кинуть потоку notify(), в то время когда он не wait()

    ВОПРОС: Хорошо ли огранизована иерархия проекта?? Файлы и папки
 */

public class Main {
    public static void main(String[] args) {
        Factory factory = new Factory();
        FabricController controller = new FabricController(factory);
        FactoryGUI gui = new FactoryGUI(controller, factory);
        factory.attach(gui);
    }
}
