import java.io.Serializable;

public class Product implements Serializable  {
    private final String name;
    private final double price;
    private int count;

    public Product(String name, double price, int count) {
        this.name = name;
        this.price = price;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }

    public void buy(int count) {
        if (count > this.count) {
            throw new RuntimeException("В наличии только " + count + " штук");
        }
        this.count -= count;
    }
    public void add(int count) {
        this.count += count;
    }
}
