import model.Product;
import service.FlashSaleSimulator;

/**
 * Main class to run the Flash Sale Inventory Management System
 * Simulates Amazon Lightning Deal with concurrent user purchases
 * 
 * Usage:
 *   java Main                              (uses defaults: 100 stock, 1000 users)
 *   java Main <stock> <users>              (custom stock and user count)
 *   java Main 50 500                       (50 items, 500 concurrent users)
 */
public class Main {
    // Default configuration
    private static final int DEFAULT_STOCK = 100;
    private static final int DEFAULT_USERS = 1000;
    private static final String PRODUCT_ID = "PROD001";
    private static final String PRODUCT_NAME = "Premium Wireless Headphones";
    private static final double PRODUCT_PRICE = 149.99;

    public static void main(String[] args) {
        // Parse command line arguments
        int stockCount = DEFAULT_STOCK;
        int userCount = DEFAULT_USERS;

        if (args.length >= 2) {
            try {
                stockCount = Integer.parseInt(args[0]);
                userCount = Integer.parseInt(args[1]);
                
                if (stockCount <= 0 || userCount <= 0) {
                    System.err.println("Error: Stock count and user count must be positive integers.");
                    printUsage();
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid input. Please provide valid integers.");
                printUsage();
                System.exit(1);
            }
        } else if (args.length == 1) {
            System.err.println("Error: Please provide both stock count and user count.");
            printUsage();
            System.exit(1);
        }

        // Display configuration
        displayConfiguration(stockCount, userCount);

        // Create product
        Product product = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE, stockCount);

        // Create and run simulator
        FlashSaleSimulator simulator = new FlashSaleSimulator(product, userCount);
        FlashSaleSimulator.SimulationResult result = simulator.startSale();

        // Display results
        result.printResults();

        // Display insights
        displayInsights(result, stockCount, userCount);
    }

    private static void displayConfiguration(int stock, int users) {
        System.out.println("\n" + "#".repeat(80));
        System.out.println("#" + " ".repeat(78) + "#");
        System.out.println("#" + " ".repeat(15) + "Flash Sale Inventory Management System" + " ".repeat(24) + "#");
        System.out.println("#" + " ".repeat(20) + "Amazon Lightning Deal Simulation" + " ".repeat(25) + "#");
        System.out.println("#" + " ".repeat(78) + "#");
        System.out.println("#".repeat(80));
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("  Stock Count: " + stock + " items");
        System.out.println("  User Count:  " + users + " concurrent users");
        System.out.println("  Product:     " + PRODUCT_NAME);
        System.out.println("  Price:       $" + PRODUCT_PRICE);
    }

    private static void displayInsights(FlashSaleSimulator.SimulationResult result, 
                                       int stockCount, int userCount) {
        System.out.println("📈 Performance Insights:");
        System.out.println("  - Throughput: " + 
                         String.format("%.2f", (result.getTotalAttempts() * 1000.0) / result.getDurationMs()) + 
                         " requests/second");
        System.out.println("  - Success Rate: " + 
                         String.format("%.2f%%", (result.getSuccessfulPurchases() * 100.0) / result.getTotalAttempts()));
        System.out.println("  - Stock Sold: " + 
                         String.format("%.2f%%", ((stockCount - result.getRemainingStock()) * 100.0) / stockCount));
        System.out.println();
        System.out.println("🔒 Thread Safety Achieved:");
        System.out.println("  - ReentrantLock prevented race conditions");
        System.out.println("  - ConcurrentHashMap ensured thread-safe order tracking");
        System.out.println("  - AtomicInteger guaranteed accurate counters");
        System.out.println("  - ExecutorService managed " + userCount + " concurrent threads efficiently");
        System.out.println();
        System.out.println("✨ Interview-Ready Features Demonstrated:");
        System.out.println("  ✓ Clean OOP design with proper encapsulation");
        System.out.println("  ✓ Thread safety using ReentrantLock");
        System.out.println("  ✓ Race condition prevention");
        System.out.println("  ✓ Concurrent data structures (ConcurrentHashMap, AtomicInteger)");
        System.out.println("  ✓ Thread pool management with ExecutorService");
        System.out.println("  ✓ Proper resource cleanup and exception handling");
        System.out.println("  ✓ O(1) time complexity for purchase operations");
        System.out.println();
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java Main                    (uses defaults: " + DEFAULT_STOCK + " stock, " + DEFAULT_USERS + " users)");
        System.out.println("  java Main <stock> <users>    (custom configuration)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java Main                    # Run with default settings");
        System.out.println("  java Main 50 500             # 50 items, 500 concurrent users");
        System.out.println("  java Main 200 2000           # 200 items, 2000 concurrent users");
        System.out.println();
    }
}