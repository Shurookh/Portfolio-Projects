package service;

import model.Order;
import model.Product;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Simulates a flash sale with multiple concurrent users
 * Uses ExecutorService thread pool for managing concurrent purchases
 */
public class FlashSaleSimulator {
    private final InventoryManager inventoryManager;
    private final int numberOfUsers;
    private final ExecutorService executorService;

    public FlashSaleSimulator(Product product, int numberOfUsers) {
        this.inventoryManager = new InventoryManager(product);
        this.numberOfUsers = numberOfUsers;
        // Create thread pool with optimal size (CPU cores * 2)
        int threadPoolSize = Math.min(numberOfUsers, Runtime.getRuntime().availableProcessors() * 2);
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Starts the flash sale simulation
     * Creates multiple threads to simulate concurrent user purchases
     * 
     * @return SimulationResult containing statistics
     */
    public SimulationResult startSale() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚡ FLASH SALE STARTED - Amazon Lightning Deal Simulation ⚡");
        System.out.println("=".repeat(80));
        System.out.println("Product: " + inventoryManager.getProduct().getName());
        System.out.println("Price: $" + String.format("%.2f", inventoryManager.getProduct().getPrice()));
        System.out.println("Total Stock: " + inventoryManager.getProduct().getTotalStock());
        System.out.println("Concurrent Users: " + numberOfUsers);
        System.out.println("=".repeat(80) + "\n");

        long startTime = System.currentTimeMillis();

        // Create list of users
        List<User> users = generateUsers(numberOfUsers);
        
        // Create list to store futures
        List<Future<Order>> futures = new ArrayList<>();

        // Submit purchase tasks for all users concurrently
        for (User user : users) {
            Future<Order> future = executorService.submit(() -> inventoryManager.processPurchase(user));
            futures.add(future);
        }

        // Wait for all tasks to complete
        for (Future<Order> future : futures) {
            try {
                future.get(); // Wait for completion
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing order: " + e.getMessage());
            }
        }

        // Shutdown executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return new SimulationResult(
                inventoryManager.getTotalAttempts(),
                inventoryManager.getSuccessfulPurchases(),
                inventoryManager.getFailedPurchases(),
                inventoryManager.getRemainingStock(),
                inventoryManager.getTotalRevenue(),
                duration
        );
    }

    /**
     * Generates list of users for simulation
     */
    private List<User> generateUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(new User("USER" + String.format("%04d", i), "User " + i));
        }
        return users;
    }

    /**
     * Inner class to hold simulation results
     */
    public static class SimulationResult {
        private final int totalAttempts;
        private final int successfulPurchases;
        private final int failedPurchases;
        private final int remainingStock;
        private final double totalRevenue;
        private final long durationMs;

        public SimulationResult(int totalAttempts, int successfulPurchases, int failedPurchases,
                                int remainingStock, double totalRevenue, long durationMs) {
            this.totalAttempts = totalAttempts;
            this.successfulPurchases = successfulPurchases;
            this.failedPurchases = failedPurchases;
            this.remainingStock = remainingStock;
            this.totalRevenue = totalRevenue;
            this.durationMs = durationMs;
        }

        public void printResults() {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("📊 FLASH SALE RESULTS");
            System.out.println("=".repeat(80));
            System.out.println("Total Purchase Attempts:    " + totalAttempts);
            System.out.println("Successful Purchases:       " + successfulPurchases + " ✅");
            System.out.println("Failed Purchases:           " + failedPurchases + " ❌");
            System.out.println("Final Remaining Stock:      " + remainingStock);
            System.out.println("Total Revenue Generated:    $" + String.format("%.2f", totalRevenue));
            System.out.println("Time Taken:                 " + durationMs + " ms (" + 
                               String.format("%.2f", durationMs / 1000.0) + " seconds)");
            System.out.println("=".repeat(80));
            
            // Verify integrity
            System.out.println("\n✓ Data Integrity Check:");
            System.out.println("  - No overselling: " + (remainingStock >= 0 ? "PASSED ✅" : "FAILED ❌"));
            System.out.println("  - Stock consistency: " + 
                             (totalAttempts == successfulPurchases + failedPurchases ? "PASSED ✅" : "FAILED ❌"));
            System.out.println("=".repeat(80) + "\n");
        }

        // Getters
        public int getTotalAttempts() { return totalAttempts; }
        public int getSuccessfulPurchases() { return successfulPurchases; }
        public int getFailedPurchases() { return failedPurchases; }
        public int getRemainingStock() { return remainingStock; }
        public double getTotalRevenue() { return totalRevenue; }
        public long getDurationMs() { return durationMs; }
    }
}