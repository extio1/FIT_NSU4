import factory.Factory;
import factory.storage.SingleSpeciesStorage;
import factory.Storage;
import factory.product.Accessory;
import factory.product.Body;
import factory.product.Engine;

public class Main {
    public static void main(String[] args) {
        Storage<Accessory> accessoryStorage = new SingleSpeciesStorage<>(10);
        Storage<Body> bodyStorage = new SingleSpeciesStorage<>(10);
        Storage<Engine> engineStorage = new SingleSpeciesStorage<>(10);
        Factory factory = new Factory();
    }
}
