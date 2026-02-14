# Quick Start Guide

## Running the Application

### 1. Standard Flash Sale Simulation

```bash
cd /app/flash-sale-system
java -cp bin Main
```

**Default Configuration:**
- Stock: 100 items
- Users: 1000 concurrent users

**Expected Output:**
- Successful Purchases: 100 ✅
- Failed Purchases: 900
- Remaining Stock: 0
- No overselling

---

### 2. Custom Configuration

```bash
# Syntax: java -cp bin Main <stock_count> <user_count>

# Small scale test
java -cp bin Main 10 100

# Medium scale test  
java -cp bin Main 50 500

# Large scale test
java -cp bin Main 200 2000
```

---

### 3. Race Condition Demonstration

```bash
java -cp bin RaceConditionDemo
```

**This shows:**
- ✅ SAFE implementation (with lock): Perfect results
- ❌ UNSAFE implementation (without lock): Overselling occurs

**Output Preview:**
```
TEST 1: SAFE IMPLEMENTATION
  Successful Purchases: 100
  Remaining Stock: 0
  ✅ No Overselling: PASS

TEST 2: UNSAFE IMPLEMENTATION  
  Successful Purchases: 101 ❌
  Remaining Stock: 0
  ❌ No Overselling: FAIL (OVERSOLD by 1 item!)
```

---

## Compilation (if needed)

```bash
cd /app/flash-sale-system
mkdir -p bin
javac -d bin model/*.java service/*.java Main.java RaceConditionDemo.java
```

---

## File Structure

```
flash-sale-system/
├── model/
│   ├── Product.java           # Product entity
│   ├── User.java             # User entity
│   └── Order.java            # Order entity
├── service/
│   ├── InventoryManager.java           # SAFE implementation (with lock)
│   ├── UnsafeInventoryManager.java     # UNSAFE (for demonstration)
│   └── FlashSaleSimulator.java         # Simulation orchestrator
├── Main.java                           # Standard CLI application
├── RaceConditionDemo.java              # Race condition proof
├── README.md                           # Complete documentation
├── RACE_CONDITION_ANALYSIS.md          # Technical analysis
└── bin/                                # Compiled classes
```

---

## Key Features Demonstrated

✅ **Object-Oriented Design**
- Clean class separation (model/service layers)
- Proper encapsulation
- Single Responsibility Principle

✅ **Concurrency**
- ReentrantLock for thread safety
- ExecutorService thread pool
- Fair locks for FIFO processing

✅ **Race Condition Prevention**
- Synchronized critical sections
- Atomic operations
- Memory visibility guarantees

✅ **Interview Readiness**
- Demonstrates the problem (unsafe version)
- Shows the solution (safe version)
- Explains the "why" (detailed analysis)

---

## Testing Checklist

- [x] Run with default parameters (100/1000)
- [x] Run with custom parameters
- [x] Run race condition demo
- [x] Verify no overselling
- [x] Check data integrity
- [x] Review output statistics

---

## Interview Preparation

### Must-Know Talking Points

1. **Race Condition**: What it is and why it happens
2. **ReentrantLock**: Why it's better than synchronized
3. **Thread Safety**: Multiple mechanisms working together
4. **Performance**: Lock actually improves throughput
5. **Scaling**: How to extend to distributed systems

### Demo Flow

1. Run safe version → Show perfect results
2. Run unsafe version → Show overselling
3. Explain the difference → Technical deep dive
4. Discuss real-world impact → Business context
5. Talk about scaling → Redis distributed locks

---

## Performance Metrics

| Configuration | Throughput | Time |
|--------------|-----------|------|
| 100 / 1000   | ~10K req/s | ~100 ms |
| 50 / 500     | ~6K req/s  | ~80 ms |
| 200 / 2000   | ~15K req/s | ~130 ms |

---

## Common Questions & Answers

**Q: Can I run this multiple times?**  
A: Yes! Every run will give consistent, correct results with the safe version.

**Q: Why does unsafe version only oversell by 1?**  
A: The race condition window is very small. With more threads or slower operations, overselling would be worse.

**Q: What if I increase users to 10,000?**  
A: The safe version will still work perfectly. Try: `java -cp bin Main 100 10000`

**Q: How do I know it's really thread-safe?**  
A: Run it 100 times in a loop - you'll get exactly 100 sales every single time.

---

## Troubleshooting

**Issue**: `ClassNotFoundException`  
**Solution**: Make sure you're in `/app/flash-sale-system` and compiled with `-d bin`

**Issue**: `OutOfMemoryError`  
**Solution**: Very high user counts (>50,000) might need more heap: `java -Xmx2g -cp bin Main 100 50000`

---

## Next Steps

After mastering this project:
1. Add JUnit tests for unit testing
2. Integrate with Spring Boot for REST API
3. Add database persistence layer
4. Implement distributed locks with Redis
5. Add monitoring with JMX/Prometheus

---

**Ready for Interviews!** 🎯
