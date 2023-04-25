package factory;

public abstract class Product{
    private final int id;

    public Product(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
