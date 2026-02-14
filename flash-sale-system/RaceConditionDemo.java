import model.Product;
import service.FlashSaleSimulator;
import service.UnsafeInventoryManager;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Demonstrates the difference between SAFE and UNSAFE implementations
 * Shows what happens when proper synchronization is missing
 */
public class RaceConditionDemo {
    
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RACE CONDITION DEMONSTRATION");
        System.out.println("Comparing SAFE vs UNSAFE implementations");
        System.out.println("=".repeat(80) + "\n");
        
        int stock = 100;
        int users = 1000;
        
        // Test 1: SAFE implementation with locks
        System.out.println("TEST 1: SAFE IMPLEMENTATION (With ReentrantLock)");
        System.out.println("-".repeat(80));
        runSafeSimulation(stock, users);
        
        System.out.println("\n\n");
        
        // Test 2: UNSAFE implementation without locks
        System.out.println("TEST 2: UNSAFE IMPLEMENTATION (Without Lock - Race Condition)");
        System.out.println("-".repeat(80));
        runUnsafeSimulation(stock, users);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ANALYSIS:");
        System.out.println("=".repeat(80));
        System.out.println("✅ SAFE version: Stock never goes negative, exact 100 successful purchases");
        System.out.println("❌ UNSAFE version: OVERSELLING occurs, stock goes negative!");
        System.out.println("\nThis demonstrates why thread synchronization is CRITICAL in");
        System.out.println("high-concurrency scenarios like flash sales.");
        System.out.println("=".repeat(80) + "\n");
    }
    
    private static void runSafeSimulation(int stock, int users) {
        Product product = new Product("PROD001", "Premium Wireless Headphones", 149.99, stock);
        FlashSaleSimulator simulator = new FlashSaleSimulator(product, users);
        FlashSaleSimulator.SimulationResult result = simulator.startSale();
        
        System.out.println("Results:");
        System.out.println("  Successful Purchases: " + result.getSuccessfulPurchases());
        System.out.println("  Failed Purchases:     " + result.getFailedPurchases());
        System.out.println("  Remaining Stock:      " + result.getRemainingStock());
        System.out.println("  Total Revenue:        $" + String.format("%.2f", result.getTotalRevenue()));
        System.out.println("  Time Taken:           " + result.getDurationMs() + " ms");
        
        boolean noOverselling = result.getRemainingStock() >= 0;
        boolean correctCount = result.getSuccessfulPurchases() == stock;
        
        System.out.println("\n  Validation:");
        System.out.println("    No Overselling:     " + (noOverselling ? "✅ PASS" : "❌ FAIL"));
        System.out.println("    Correct Count:      " + (correctCount ? "✅ PASS" : "❌ FAIL"));
    }
    
    private static void runUnsafeSimulation(int stock, int users) {
        Product product = new Product("PROD001", "Premium Wireless Headphones", 149.99, stock);
        UnsafeInventoryManager unsafeManager = new UnsafeInventoryManager(product);
        
        int threadPoolSize = Math.min(users, Runtime.getRuntime().availableProcessors() * 2);
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        
        long startTime = System.currentTimeMillis();
        
        List<User> userList = new ArrayList<>();
        for (int i = 1; i <= users; i++) {
            userList.add(new User("USER" + String.format("%04d", i), "User " + i));
        }
        
        List<Future<?>> futures = new ArrayList<>();
        for (User user : userList) {
            Future<?> future = executorService.submit(() -> unsafeManager.processPurchase(user));
            futures.add(future);
        }
        
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                // ignore
            }
        }
        
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Results:");
        System.out.println("  Successful Purchases: " + unsafeManager.getSuccessfulPurchases());
        System.out.println("  Failed Purchases:     " + unsafeManager.getFailedPurchases());
        System.out.println("  Remaining Stock:      " + unsafeManager.getRemainingStock());
        System.out.println("  Total Revenue:        $" + String.format("%.2f", unsafeManager.getTotalRevenue()));
        System.out.println("  Time Taken:           " + (endTime - startTime) + " ms");
        
        boolean noOverselling = unsafeManager.getRemainingStock() >= 0;
        boolean correctCount = unsafeManager.getSuccessfulPurchases() == stock;
        
        System.out.println("\n  Validation:");
        System.out.println("    No Overselling:     " + (noOverselling ? "✅ PASS" : "❌ FAIL (NEGATIVE STOCK!)"));
        System.out.println("    Correct Count:      " + (correctCount ? "✅ PASS" : "❌ FAIL (OVERSOLD!)"));
        
        if (!noOverselling || !correctCount) {
            System.out.println("\n  ⚠️  CRITICAL ISSUE DETECTED:");
            System.out.println("      Expected sales: " + stock);
            System.out.println("      Actual sales:   " + unsafeManager.getSuccessfulPurchases());
            System.out.println("      Oversold by:    " + (unsafeManager.getSuccessfulPurchases() - stock) + " items");
            System.out.println("      This is a RACE CONDITION causing data corruption!");
        }
    }
}
