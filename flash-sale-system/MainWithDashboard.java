import model.Product;
import service.FlashSaleSimulator;
import service.DashboardServer;
import service.UnsafeInventoryManager;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Main class with integrated dashboard for visual analytics
 * Runs simulations and displays results in a beautiful web interface
 */
public class MainWithDashboard {
    private static final int DASHBOARD_PORT = 8080;
    private static DashboardServer dashboardServer;
    
    public static void main(String[] args) {
        displayBanner();
        
        try {
            // Start dashboard server
            dashboardServer = new DashboardServer(DASHBOARD_PORT);
            dashboardServer.start();
            
            // Interactive menu
            Scanner scanner = new Scanner(System.in);
            boolean running = true;
            
            while (running) {
                displayMenu();
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        runSafeSimulation(100, 1000);
                        break;
                    case "2":
                        runCustomSimulation(scanner);
                        break;
                    case "3":
                        runComparison();
                        break;
                    case "4":
                        runMultipleSimulations();
                        break;
                    case "5":
                        System.out.println("\nOpening dashboard in browser...");
                        System.out.println("URL: http://localhost:" + DASHBOARD_PORT);
                        break;
                    case "6":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
            
            System.out.println("\nShutting down dashboard server...");
            dashboardServer.stop();
            System.out.println("Goodbye!\n");
            
        } catch (IOException e) {
            System.err.println("Error starting dashboard server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void displayBanner() {
        System.out.println("\n" + "#".repeat(80));
        System.out.println("#" + " ".repeat(78) + "#");
        System.out.println("#" + " ".repeat(15) + "Flash Sale System with Analytics Dashboard" + " ".repeat(20) + "#");
        System.out.println("#" + " ".repeat(20) + "Amazon Lightning Deal Simulation" + " ".repeat(25) + "#");
        System.out.println("#" + " ".repeat(78) + "#");
        System.out.println("#".repeat(80));
        System.out.println();
    }
    
    private static void displayMenu() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 FLASH SALE DASHBOARD - INTERACTIVE MENU");
        System.out.println("=".repeat(80));
        System.out.println("1. Run Standard Simulation (100 stock, 1000 users)");
        System.out.println("2. Run Custom Simulation (specify parameters)");
        System.out.println("3. Run Safe vs Unsafe Comparison");
        System.out.println("4. Run Multiple Simulations (5 runs)");
        System.out.println("5. View Dashboard URL");
        System.out.println("6. Exit");
        System.out.println("=".repeat(80));
        System.out.print("Enter your choice (1-6): ");
    }
    
    private static void runSafeSimulation(int stock, int users) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Running SAFE simulation...");
        System.out.println("=".repeat(80));
        
        Product product = new Product("PROD001", "Premium Wireless Headphones", 149.99, stock);
        FlashSaleSimulator simulator = new FlashSaleSimulator(product, users);
        FlashSaleSimulator.SimulationResult result = simulator.startSale();
        result.printResults();
        
        // Add to dashboard
        DashboardServer.SimulationData data = new DashboardServer.SimulationData(
            stock, users,
            result.getSuccessfulPurchases(),
            result.getFailedPurchases(),
            result.getRemainingStock(),
            result.getTotalRevenue(),
            result.getDurationMs(),
            true
        );
        dashboardServer.addSimulation(data);
        
        System.out.println("✅ Data added to dashboard. Visit http://localhost:" + DASHBOARD_PORT);
    }
    
    private static void runCustomSimulation(Scanner scanner) {
        System.out.print("\nEnter stock count: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter number of users: ");
        int users = Integer.parseInt(scanner.nextLine().trim());
        
        runSafeSimulation(stock, users);
    }
    
    private static void runComparison() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Running SAFE vs UNSAFE comparison...");
        System.out.println("=".repeat(80));
        
        int stock = 100;
        int users = 1000;
        
        // Safe version
        System.out.println("\n1️⃣  SAFE Implementation (With Lock)");
        System.out.println("-".repeat(80));
        Product safeProduct = new Product("PROD001", "Premium Wireless Headphones", 149.99, stock);
        FlashSaleSimulator safeSimulator = new FlashSaleSimulator(safeProduct, users);
        FlashSaleSimulator.SimulationResult safeResult = safeSimulator.startSale();
        
        System.out.println("Successful: " + safeResult.getSuccessfulPurchases() + " | " +
                         "Failed: " + safeResult.getFailedPurchases() + " | " +
                         "Time: " + safeResult.getDurationMs() + "ms");
        
        DashboardServer.SimulationData safeData = new DashboardServer.SimulationData(
            stock, users, safeResult.getSuccessfulPurchases(),
            safeResult.getFailedPurchases(), safeResult.getRemainingStock(),
            safeResult.getTotalRevenue(), safeResult.getDurationMs(), true
        );
        dashboardServer.addSimulation(safeData);
        
        // Unsafe version
        System.out.println("\n2️⃣  UNSAFE Implementation (Without Lock)");
        System.out.println("-".repeat(80));
        Product unsafeProduct = new Product("PROD001", "Premium Wireless Headphones", 149.99, stock);
        UnsafeInventoryManager unsafeManager = new UnsafeInventoryManager(unsafeProduct);
        
        ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(users, Runtime.getRuntime().availableProcessors() * 2)
        );
        
        long startTime = System.currentTimeMillis();
        List<User> userList = new ArrayList<>();
        for (int i = 1; i <= users; i++) {
            userList.add(new User("USER" + String.format("%04d", i), "User " + i));
        }
        
        List<Future<?>> futures = new ArrayList<>();
        for (User user : userList) {
            futures.add(executor.submit(() -> unsafeManager.processPurchase(user)));
        }
        
        for (Future<?> future : futures) {
            try { future.get(); } catch (Exception e) { }
        }
        
        executor.shutdown();
        try { executor.awaitTermination(60, TimeUnit.SECONDS); } catch (Exception e) { }
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Successful: " + unsafeManager.getSuccessfulPurchases() + " | " +
                         "Failed: " + unsafeManager.getFailedPurchases() + " | " +
                         "Time: " + duration + "ms");
        
        DashboardServer.SimulationData unsafeData = new DashboardServer.SimulationData(
            stock, users, unsafeManager.getSuccessfulPurchases(),
            unsafeManager.getFailedPurchases(), unsafeManager.getRemainingStock(),
            unsafeManager.getTotalRevenue(), duration, false
        );
        dashboardServer.addSimulation(unsafeData);
        
        // Comparison summary
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 COMPARISON RESULTS");
        System.out.println("=".repeat(80));
        System.out.println(String.format("%-30s | %-20s | %-20s", "Metric", "Safe (Lock)", "Unsafe (No Lock)"));
        System.out.println("-".repeat(80));
        System.out.println(String.format("%-30s | %-20d | %-20d", "Successful Purchases",
            safeResult.getSuccessfulPurchases(), unsafeManager.getSuccessfulPurchases()));
        System.out.println(String.format("%-30s | %-20s | %-20s", "Overselling",
            safeResult.getSuccessfulPurchases() == stock ? "✅ No" : "❌ Yes",
            unsafeManager.getSuccessfulPurchases() == stock ? "✅ No" : "❌ Yes"));
        System.out.println(String.format("%-30s | %-20dms | %-20dms", "Execution Time",
            safeResult.getDurationMs(), duration));
        System.out.println("=".repeat(80));
        
        System.out.println("\n✅ Comparison data added to dashboard!");
    }
    
    private static void runMultipleSimulations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Running 5 simulations for trend analysis...");
        System.out.println("=".repeat(80));
        
        int[] stockLevels = {50, 75, 100, 150, 200};
        int users = 1000;
        
        for (int i = 0; i < stockLevels.length; i++) {
            System.out.println("\nRun " + (i + 1) + "/5 - Stock: " + stockLevels[i]);
            runSafeSimulation(stockLevels[i], users);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("\n✅ All simulations complete! Check dashboard for trend visualization.");
    }
}
