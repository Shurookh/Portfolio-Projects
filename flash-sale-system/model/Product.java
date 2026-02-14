package model;

/**
 * Represents a product in the flash sale
 * Contains product details and stock management
 */
public class Product {
    private final String productId;
    private final String name;
    private final double price;
    private final int totalStock;
    private int remainingStock;

    public Product(String productId, String name, double price, int totalStock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.totalStock = totalStock;
        this.remainingStock = totalStock;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public int getRemainingStock() {
        return remainingStock;
    }

    /**
     * Decreases the remaining stock by 1
     * This method should only be called within synchronized context
     */
    public void decrementStock() {
        if (remainingStock > 0) {
            remainingStock--;
        }
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, totalStock=%d, remainingStock=%d}",
                productId, name, price, totalStock, remainingStock);
    }
}