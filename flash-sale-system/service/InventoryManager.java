package service;

import model.Order;
import model.Product;
import model.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages inventory and order processing with thread safety
 * Uses ReentrantLock to prevent race conditions
 * Ensures no overselling and maintains data consistency
 */
public class InventoryManager {
    private final Product product;
    private final ReentrantLock lock;
    private final ConcurrentHashMap<String, Order> orders;
    private final ConcurrentHashMap<String, Boolean> userPurchaseMap;
    private final AtomicInteger orderCounter;
    private final AtomicInteger attemptCounter;
    private final AtomicInteger successCounter;
    private final AtomicInteger failureCounter;

    public InventoryManager(Product product) {
        this.product = product;
        this.lock = new ReentrantLock(true); // Fair lock for FIFO processing
        this.orders = new ConcurrentHashMap<>();
        this.userPurchaseMap = new ConcurrentHashMap<>();
        this.orderCounter = new AtomicInteger(1);
        this.attemptCounter = new AtomicInteger(0);
        this.successCounter = new AtomicInteger(0);
        this.failureCounter = new AtomicInteger(0);
    }

    /**
     * Attempts to process a purchase request
     * Thread-safe method using ReentrantLock
     * 
     * Time Complexity: O(1) for HashMap operations
     * Space Complexity: O(n) where n is number of unique users
     * 
     * @param user The user attempting to purchase
     * @return Order object with SUCCESS or FAILED status
     */
    public Order processPurchase(User user) {
        attemptCounter.incrementAndGet();
        String orderId = "ORD" + String.format("%06d", orderCounter.getAndIncrement());
        
        // Acquire lock to ensure thread safety
        lock.lock();
        try {
            // Check if user has already purchased (1 purchase per user limit)
            if (userPurchaseMap.containsKey(user.getUserId())) {
                failureCounter.incrementAndGet();
                return createOrder(orderId, user, Order.OrderStatus.FAILED);
            }

            // Check if stock is available
            if (product.getRemainingStock() <= 0) {
                failureCounter.incrementAndGet();
                return createOrder(orderId, user, Order.OrderStatus.FAILED);
            }

            // Process successful purchase
            product.decrementStock();
            userPurchaseMap.put(user.getUserId(), true);
            successCounter.incrementAndGet();
            return createOrder(orderId, user, Order.OrderStatus.SUCCESS);

        } finally {
            // Always release lock in finally block
            lock.unlock();
        }
    }

    /**
     * Creates and stores an order
     */
    private Order createOrder(String orderId, User user, Order.OrderStatus status) {
        Order order = new Order(orderId, user.getUserId(), product.getProductId(), status);
        orders.put(orderId, order);
        return order;
    }

    // Getters for statistics
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

    public ConcurrentHashMap<String, Order> getOrders() {
        return orders;
    }
}