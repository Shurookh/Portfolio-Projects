package service;

import model.Order;
import model.Product;
import model.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UNSAFE VERSION - Demonstrates what happens WITHOUT proper synchronization
 * This class intentionally has race conditions to show the problem
 * 
 * WARNING: This will cause overselling and data corruption!
 */
public class UnsafeInventoryManager {
    private final Product product;
    private final ConcurrentHashMap<String, Order> orders;
    private final ConcurrentHashMap<String, Boolean> userPurchaseMap;
    private final AtomicInteger orderCounter;
    private final AtomicInteger attemptCounter;
    private final AtomicInteger successCounter;
    private final AtomicInteger failureCounter;

    public UnsafeInventoryManager(Product product) {
        this.product = product;
        this.orders = new ConcurrentHashMap<>();
        this.userPurchaseMap = new ConcurrentHashMap<>();
        this.orderCounter = new AtomicInteger(1);
        this.attemptCounter = new AtomicInteger(0);
        this.successCounter = new AtomicInteger(0);
        this.failureCounter = new AtomicInteger(0);
    }

    /**
     * UNSAFE METHOD - No lock protection!
     * Multiple threads can execute this simultaneously
     * causing race conditions
     */
    public Order processPurchase(User user) {
        attemptCounter.incrementAndGet();
        String orderId = "ORD" + String.format("%06d", orderCounter.getAndIncrement());
        
        // NO LOCK HERE - RACE CONDITION WILL OCCUR!
        
        // Check if user has already purchased
        if (userPurchaseMap.containsKey(user.getUserId())) {
            failureCounter.incrementAndGet();
            return createOrder(orderId, user, Order.OrderStatus.FAILED);
        }

        // RACE CONDITION: Multiple threads read the same stock value
        int currentStock = product.getRemainingStock();
        
        // Small delay to increase chance of race condition
        try {
            Thread.sleep(0, 100); // 100 nanoseconds
        } catch (InterruptedException e) {
            // ignore
        }
        
        if (currentStock <= 0) {
            failureCounter.incrementAndGet();
            return createOrder(orderId, user, Order.OrderStatus.FAILED);
        }

        // RACE CONDITION: Multiple threads decrement simultaneously
        product.decrementStock();
        userPurchaseMap.put(user.getUserId(), true);
        successCounter.incrementAndGet();
        return createOrder(orderId, user, Order.OrderStatus.SUCCESS);
    }

    private Order createOrder(String orderId, User user, Order.OrderStatus status) {
        Order order = new Order(orderId, user.getUserId(), product.getProductId(), status);
        orders.put(orderId, order);
        return order;
    }

    public int getTotalAttempts() {
        return attemptCounter.get();
    }

    public int getSuccessfulPurchases() {
        return successCounter.get();
    }

    public int getFailedPurchases() {
        return failureCounter.get();
    }

    public int getRemainingStock() {
        return product.getRemainingStock();
    }

    public double getTotalRevenue() {
        return successCounter.get() * product.getPrice();
    }

    public Product getProduct() {
        return product;
    }
}
