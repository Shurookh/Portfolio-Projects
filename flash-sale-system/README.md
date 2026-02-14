# Flash Sale Inventory Management System

## Amazon Lightning Deal Simulation

A robust, thread-safe Java application demonstrating high-traffic flash sale inventory management with concurrent user purchases. This project showcases strong Object-Oriented Programming principles, Java multithreading, race condition handling, and clean architecture.

---

## 🎯 Project Overview

This system simulates a real-world flash sale scenario (like Amazon Lightning Deals) where:
- **1 product** with limited stock (configurable, default: 100 items)
- **Multiple users** (configurable, default: 1000) attempting to purchase simultaneously
- **Thread-safe operations** to prevent overselling
- **1 purchase limit per user** to ensure fairness
- **Real-time statistics** and performance metrics

### Key Features
- ✅ Prevents race conditions using `ReentrantLock`
- ✅ Thread-safe order tracking with `ConcurrentHashMap`
- ✅ Efficient concurrent processing using `ExecutorService`
- ✅ Atomic counters for accurate statistics
- ✅ Configurable via CLI arguments
- ✅ Clean, modular, interview-ready code

---

## 🏗️ Architecture & Design

### Class Structure

```
flash-sale-system/
├── model/
│   ├── Product.java              # Product entity with stock management
│   ├── User.java                 # User entity
│   └── Order.java                # Order entity with status tracking
├── service/
│   ├── InventoryManager.java     # Core business logic with thread safety
│   └── FlashSaleSimulator.java   # Simulation orchestration
├── Main.java                     # Entry point with CLI support
└── README.md                     # Documentation
```

### Design Principles

1. **Separation of Concerns**
   - `model/` - Data entities (Product, User, Order)
   - `service/` - Business logic (InventoryManager, FlashSaleSimulator)
   - `Main.java` - Application entry and orchestration

2. **Encapsulation**
   - Private fields with public getters
   - Immutable where appropriate (final fields)
   - Clear method contracts

3. **Thread Safety**
   - `ReentrantLock` for critical section protection
   - `ConcurrentHashMap` for thread-safe collections
   - `AtomicInteger` for thread-safe counters

4. **Clean Code**
   - Descriptive naming conventions
   - Comprehensive comments
   - Single Responsibility Principle

---

## 🔒 Concurrency & Thread Safety

### Race Condition Problem

**What is a Race Condition?**

A race condition occurs when multiple threads access shared data concurrently, and at least one thread modifies the data. The final result depends on the unpredictable timing of thread execution.

**Example Without Synchronization:**
```
Thread 1: Read stock = 1
Thread 2: Read stock = 1
Thread 1: Decrement stock to 0, process order ✅
Thread 2: Decrement stock to -1, process order ✅ (OVERSELLING!)
```

**Result:** 2 successful orders but only 1 item in stock = DATA CORRUPTION

### Solution Implementation

#### 1. ReentrantLock (Primary Mechanism)

```java
private final ReentrantLock lock = new ReentrantLock(true);

public Order processPurchase(User user) {
    lock.lock();  // Acquire lock - only one thread can proceed
    try {
        // Critical section - check stock and process purchase
        if (product.getRemainingStock() > 0) {
            product.decrementStock();
            // Create successful order
        }
    } finally {
        lock.unlock();  // Always release lock
    }
}
```

**Benefits of ReentrantLock:**
- **Fairness**: Fair lock ensures FIFO processing (first-come-first-served)
- **Flexibility**: Can attempt lock without blocking
- **Control**: Explicit lock/unlock provides better control than `synchronized`
- **Interruptibility**: Can interrupt threads waiting for lock

#### 2. ConcurrentHashMap

```java
private final ConcurrentHashMap<String, Order> orders;
private final ConcurrentHashMap<String, Boolean> userPurchaseMap;
```

**Why ConcurrentHashMap?**
- Thread-safe without external synchronization
- Better performance than `Hashtable` or `Collections.synchronizedMap()`
- Lock striping - only locks affected segments
- Atomic operations like `putIfAbsent()`

#### 3. AtomicInteger

```java
private final AtomicInteger successCounter = new AtomicInteger(0);
successCounter.incrementAndGet();  // Thread-safe increment
```

**Benefits:**
- Lock-free atomic operations
- Better performance than synchronized blocks for simple counters
- CAS (Compare-And-Swap) operations

#### 4. ExecutorService Thread Pool

```java
int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
```

**Advantages:**
- Reuses threads (avoids overhead of creating new threads)
- Controls maximum concurrent threads
- Automatic queue management
- Graceful shutdown mechanism

---

## ⏱️ Time Complexity Analysis

### InventoryManager.processPurchase()

**Time Complexity: O(1)**

```java
public Order processPurchase(User user) {
    lock.lock();                                    // O(1) - acquire lock
    try {
        if (userPurchaseMap.containsKey(userId))    // O(1) - HashMap lookup
        if (product.getRemainingStock() <= 0)       // O(1) - field access
        product.decrementStock();                    // O(1) - simple decrement
        userPurchaseMap.put(userId, true);          // O(1) - HashMap insert
        orders.put(orderId, order);                  // O(1) - HashMap insert
    } finally {
        lock.unlock();                               // O(1) - release lock
    }
}
```

**All operations are O(1):**
- Lock acquisition/release: O(1)
- HashMap operations (get, put, containsKey): O(1) average case
- Counter increments: O(1)
- Stock decrement: O(1)

**Space Complexity: O(n)** where n = number of unique users
- `orders` map stores up to n orders
- `userPurchaseMap` stores up to n user entries

---

## 🌐 Scaling to Distributed Systems

### Current Limitations (Single Machine)

This implementation works perfectly for a single-server deployment but faces challenges in distributed environments:

1. **Shared State Problem**: Multiple application instances can't share the same in-memory lock
2. **Race Conditions Across Servers**: Servers A and B might both process the same purchase
3. **Data Inconsistency**: No centralized coordination

### Distributed System Solution: Redis Distributed Locking

#### Why Redis?
- **In-memory speed**: Microsecond latency
- **Atomic operations**: Built-in atomic commands
- **Distributed coordination**: Single source of truth
- **High availability**: Can be clustered

#### Implementation Approach

**1. Redlock Algorithm (Industry Standard)**

```java
// Conceptual implementation
public class DistributedInventoryManager {
    private final RedissonClient redisson;
    
    public Order processPurchase(User user, Product product) {
        // Create distributed lock key
        String lockKey = "lock:product:" + product.getProductId();
        RLock lock = redisson.getLock(lockKey);
        
        try {
            // Try to acquire lock with timeout
            boolean acquired = lock.tryLock(100, 10000, TimeUnit.MILLISECONDS);
            
            if (!acquired) {
                return createFailedOrder("Lock acquisition timeout");
            }
            
            // Critical section - same business logic
            String stockKey = "stock:" + product.getProductId();
            Long currentStock = redisson.getAtomicLong(stockKey).get();
            
            if (currentStock <= 0) {
                return createFailedOrder("Out of stock");
            }
            
            // Atomic decrement
            redisson.getAtomicLong(stockKey).decrementAndGet();
            
            return createSuccessfulOrder(user, product);
            
        } finally {
            lock.unlock();
        }
    }
}
```

**2. Key Components**

```java
// Redis keys design
"lock:product:{productId}"           // Distributed lock
"stock:product:{productId}"          // Atomic stock counter
"purchases:user:{userId}"            // User purchase tracking
"orders:{orderId}"                   // Order details
```

**3. Lock Properties**

```java
RLock lock = redisson.getFairLock(lockKey);  // Fair lock for FIFO
lock.tryLock(
    waitTime: 100,       // Max wait to acquire lock (ms)
    leaseTime: 10000,    // Auto-release after 10 seconds (prevents deadlock)
    TimeUnit.MILLISECONDS
);
```

#### Advanced Distributed Patterns

**1. Inventory Reservation Pattern**
```
1. Reserve inventory temporarily (soft lock)
2. Process payment
3. Confirm reservation (hard lock)
4. If payment fails, release reservation
```

**2. Event-Driven Architecture**
```
User Request → Queue (Kafka/RabbitMQ) → Worker Pool → Redis Lock → Process
```

**3. Optimistic Locking with Versioning**
```java
// Redis WATCH command for optimistic locking
WATCH stock:product:123
GET stock:product:123
// Check stock
MULTI
DECR stock:product:123
EXEC  // Only succeeds if stock wasn't modified
```

#### Performance Comparison

| Approach | Throughput | Consistency | Complexity |
|----------|-----------|-------------|------------|
| Single Server Lock | Very High | Strong | Low |
| Redis Lock | High | Strong | Medium |
| Database Lock | Medium | Strong | Low |
| Optimistic Lock | Very High | Eventual | High |

#### Real-World Examples

**Amazon's Approach:**
- Uses DynamoDB with conditional writes
- Optimistic locking with version numbers
- Queue-based inventory reservation

**Flipkart's BigBillionDay:**
- Redis-based distributed locks
- Multi-layered caching
- Inventory reservation with TTL

**Alibaba Singles' Day:**
- Custom distributed coordination (Tair)
- Tiered inventory management
- Pre-allocation strategies

#### Migration Path: Single Server → Distributed

```
Phase 1: Current (Single Server)
  ↓
Phase 2: Add Redis for session/cache
  ↓
Phase 3: Move inventory state to Redis
  ↓
Phase 4: Implement distributed locks
  ↓
Phase 5: Scale horizontally with load balancer
```

---

## 🚀 How to Run

### Compile

```bash
cd /app/flash-sale-system
javac -d bin model/*.java service/*.java Main.java MainWithDashboard.java
```

### Option 1: CLI Mode (Simple)

#### Run with Defaults (100 stock, 1000 users)

```bash
java -cp bin Main
```

#### Run with Custom Parameters

```bash
# Syntax: java -cp bin Main <stock_count> <user_count>

java -cp bin Main 50 500      # 50 items, 500 users
java -cp bin Main 200 2000    # 200 items, 2000 users
java -cp bin Main 10 100      # 10 items, 100 users
```

### Option 2: Dashboard Mode (Visual Analytics) 📊

#### Run with Interactive Dashboard

```bash
java -cp bin MainWithDashboard
```

**Features:**
- 🌐 Beautiful web-based dashboard at `http://localhost:8080`
- 📊 Real-time charts and visualizations
- 📈 Performance metrics tracking
- 🔄 Auto-refreshing data
- 📉 Historical trend analysis
- ⚖️ Safe vs Unsafe comparison charts

**Interactive Menu:**
1. Run Standard Simulation (100 stock, 1000 users)
2. Run Custom Simulation (specify parameters)
3. Run Safe vs Unsafe Comparison
4. Run Multiple Simulations (5 runs)
5. View Dashboard URL
6. Exit

See [DASHBOARD_GUIDE.md](DASHBOARD_GUIDE.md) for detailed dashboard documentation.

---

## 📊 Sample Output

```
################################################################################
#                                                                              #
#               Flash Sale Inventory Management System                        #
#                    Amazon Lightning Deal Simulation                          #
#                                                                              #
################################################################################

Configuration:
  Stock Count: 100 items
  User Count:  1000 concurrent users
  Product:     Premium Wireless Headphones
  Price:       $149.99

================================================================================
⚡ FLASH SALE STARTED - Amazon Lightning Deal Simulation ⚡
================================================================================
Product: Premium Wireless Headphones
Price: $149.99
Total Stock: 100
Concurrent Users: 1000
================================================================================

================================================================================
📊 FLASH SALE RESULTS
================================================================================
Total Purchase Attempts:    1000
Successful Purchases:       100 ✅
Failed Purchases:           900 ❌
Final Remaining Stock:      0
Total Revenue Generated:    $14999.00
Time Taken:                 45 ms (0.05 seconds)
================================================================================

✓ Data Integrity Check:
  - No overselling: PASSED ✅
  - Stock consistency: PASSED ✅
================================================================================

📈 Performance Insights:
  - Throughput: 22222.22 requests/second
  - Success Rate: 10.00%
  - Stock Sold: 100.00%

🔒 Thread Safety Achieved:
  - ReentrantLock prevented race conditions
  - ConcurrentHashMap ensured thread-safe order tracking
  - AtomicInteger guaranteed accurate counters
  - ExecutorService managed 1000 concurrent threads efficiently

✨ Interview-Ready Features Demonstrated:
  ✓ Clean OOP design with proper encapsulation
  ✓ Thread safety using ReentrantLock
  ✓ Race condition prevention
  ✓ Concurrent data structures (ConcurrentHashMap, AtomicInteger)
  ✓ Thread pool management with ExecutorService
  ✓ Proper resource cleanup and exception handling
  ✓ O(1) time complexity for purchase operations
```

---

## 🎓 Interview Discussion Points

### Technical Questions You Can Answer:

1. **"Why use ReentrantLock instead of synchronized?"**
   - Fair locks for FIFO processing
   - Interruptibility
   - Try-lock capability
   - Better control and flexibility

2. **"How do you prevent race conditions?"**
   - Critical section protection with locks
   - Atomic operations for counters
   - Thread-safe collections
   - Proper locking hierarchy

3. **"What happens if a thread crashes while holding the lock?"**
   - Use try-finally to guarantee unlock
   - Lock timeout mechanisms
   - Monitoring and alerting

4. **"How would you scale this to handle millions of users?"**
   - Distribute using Redis locks
   - Event-driven architecture with queues
   - Horizontal scaling with load balancers
   - Caching layers
   - Database sharding

5. **"What's the time complexity of your solution?"**
   - O(1) for purchase processing
   - O(n) space for tracking users
   - Constant time HashMap operations

6. **"How do you ensure no overselling?"**
   - Atomic stock checks within lock
   - Lock before read-modify-write
   - Data integrity validations

---

## 🏆 Why This Project is Interview-Ready

### ✅ Amazon SDE Interview Alignment

1. **Object-Oriented Design**
   - Clear class hierarchy
   - Proper encapsulation
   - Single Responsibility Principle

2. **Concurrency Expertise**
   - Real-world multithreading scenario
   - Multiple synchronization techniques
   - Thread pool management

3. **System Design Understanding**
   - Scalability discussion (distributed systems)
   - Performance optimization
   - Trade-off analysis

4. **Problem-Solving Skills**
   - Identifies race conditions
   - Implements robust solutions
   - Handles edge cases

5. **Code Quality**
   - Clean, readable code
   - Comprehensive documentation
   - Professional structure

---

## 📚 Key Technologies Used

- **Java 17**: Modern Java features
- **java.util.concurrent**: ExecutorService, ReentrantLock, ConcurrentHashMap
- **java.util.concurrent.atomic**: AtomicInteger
- **java.time**: LocalDateTime for timestamps

---

## 🔧 Extension Ideas

1. **Add Logging**
   - SLF4J + Logback
   - Track detailed transaction logs

2. **Persistence Layer**
   - Store orders in database
   - Add H2 or MySQL integration

3. **REST API**
   - Expose via Spring Boot
   - Real-time WebSocket updates

4. **Monitoring**
   - Add JMX metrics
   - Prometheus integration

5. **Testing**
   - Unit tests with JUnit 5
   - Concurrent testing with stress tests

---

## 📝 License

This project is created for educational and interview preparation purposes.

---

## 👤 Author

Created as an interview-ready project demonstrating Java concurrency and system design skills.

---

## 🙏 Acknowledgments

Inspired by real-world flash sale systems used by Amazon, Flipkart, and Alibaba.
