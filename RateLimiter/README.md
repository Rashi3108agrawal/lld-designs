# 🚦 Rate Limiter – Low Level Design

A **high-performance rate limiter** that controls the number of requests per user/IP to prevent abuse, DDoS attacks, and API throttling. Achieves **O(1) decision making** and supports both local and distributed scenarios.

---

## 📌 Problem Statement

Design a **Rate Limiter** for an API endpoint that:

- ✅ Limits number of requests per user/IP in a time window
- ✅ Makes allow/deny decisions in **O(1) time**
- ✅ Prevents DDoS attacks and API abuse
- ✅ Works in **distributed environments** (multiple servers)
- ✅ Supports **high concurrency** without contention
- ✅ Respects burst traffic (optional, depends on algorithm)

---

## 🎯 Core Requirements

### Functional Requirements
1. Allow/deny incoming requests based on limit
2. Support per-user AND per-IP rate limiting
3. Return request quota information
4. Work in distributed systems (multiple servers)
5. Support burst traffic (depends on algorithm)

### Non-Functional Requirements
1. **Performance**: O(1) decision per request
2. **Scalability**: Handle millions of requests per second
3. **Consistency**: Accurate across distributed systems
4. **Low Latency**: Sub-millisecond decision making
5. **Memory Efficient**: Minimal memory footprint
6. **Fairness**: Each user gets their fair quota

---

## ⚙️ Core Algorithms

### **1. 🪣 Token Bucket Algorithm (BEST FOR APIS)**

#### Idea
```
- Bucket has fixed capacity (max tokens)
- Tokens refill at constant rate
- Each request consumes 1 token
- If tokens available → ALLOW
- If no tokens → REJECT

Example:
Capacity: 100 tokens
Refill rate: 10 tokens/second
```

#### Visual Representation
```
Time 0s:    [●●●●●●●●●●] = 100 tokens
            Request → ALLOW (99 left)
            Request → ALLOW (98 left)

Time 1s:    Refill 10 tokens
            [●●●●●●●●●●●●●●●●●●●●] = 108 (capped at 100)
            
Time 1.5s:  Request → ALLOW (99 left)
```

#### Advantages ✅
- Allows burst traffic
- Simple O(1) logic
- Memory efficient
- Works well for APIs

#### Disadvantages ❌
- Accuracy concerns with estimation
- Burst can overwhelm downstream services

#### When to Use
- REST APIs with burst traffic
- Payment gateways
- Search APIs

---

### **2. 🪟 Sliding Window Log (MOST ACCURATE)**

#### Idea
```
- Store timestamp of every request
- Remove requests older than time window
- Count remaining requests
- If count < limit → ALLOW

Example (limit = 3, window = 1 minute):
Time 0:10:  [0:10] (count = 1)
Time 0:20:  [0:10, 0:20] (count = 2)
Time 0:30:  [0:10, 0:20, 0:30] (count = 3)
Time 0:40:  Request rejected (count = 3 already)
Time 1:15:  [0:20, 0:30] remove older → [0:20, 0:30] (count = 2)
            → ALLOW new request
```

#### Advantages ✅
- Very accurate
- No approximation errors
- Perfect fairness

#### Disadvantages ❌
- High memory (O(n) per user)
- O(n) time complexity
- Slow under heavy load

#### When to Use
- Billing systems (strict accuracy needed)
- Rate limiting for payments
- Compliance-heavy systems

---

### **3. 📊 Sliding Window Counter (GOOD BALANCE)**

#### Idea
```
- Divide time into buckets (1 second intervals)
- Keep count per bucket
- Sum recent buckets in window
- O(1) performance with O(m) space (m = buckets)

Example (limit = 100 req/min, bucket = 1 sec):
Minute 1: [10, 20, 15, 25, 20, 10] buckets
          = 100 requests total (allowed)

Second 61: New bucket starts, oldest drops
New total: [20, 15, 25, 20, 10, 5] = 95 requests
          → Allow more requests
```

#### Advantages ✅
- O(1) operations
- Reasonable memory
- Good approximation
- Fast under load

#### Disadvantages ❌
- Edge case: Requests at bucket boundaries
- Slight inaccuracy possible

#### When to Use
- General API rate limiting
- Web service throttling
- Load balancing

---

### **4. 🪣 Leaky Bucket (TRAFFIC SHAPING)**

#### Idea
```
- Requests enter queue
- Processed at constant rate (leak rate)
- Overflow requests dropped
- Ensures smooth output

Example:
Leak rate: 10 requests/second
Queue capacity: 100

[●●●●●●●●●●|||||||||||||||||||||...]
↓ (1 request leaks every 100ms)
```

#### Advantages ✅
- Smooth, predictable traffic
- No bursts allowed
- Good for shaping

#### Disadvantages ❌
- No burst handling
- Dropped requests
- Less flexible

#### When to Use
- Network traffic shaping
- Egress rate limiting
- Smooth streaming

---

## 🧠 Algorithm Comparison

| Feature | Token Bucket | Sliding Window Log | Sliding Window Counter | Leaky Bucket |
|---------|-------------|-------------------|----------------------|--------------|
| **Accuracy** | Medium | ✅ High | Medium | High |
| **Time Complexity** | O(1) | ❌ O(n) | ✅ O(1) | O(1) |
| **Space Complexity** | ✅ O(1) | ❌ O(n) | O(m) | O(1) |
| **Burst Handling** | ✅ Yes | ❌ No | ❌ No | ❌ No |
| **Use Case** | 🔥 APIs | 💰 Billing | 📊 General | 🌊 Shaping |

### **Recommendation for Interviews**
> **Token Bucket is preferred for APIs** (balance of features)  
> **Sliding Window Log for strict billing**

---

## 🔥 Token Bucket Deep Dive

### **How It Works**

```
Step 1: Initialize
capacity = 100
currentTokens = 100
refillRate = 10 (per second)
lastRefillTime = now

Step 2: Request comes
tokensToAdd = (now - lastRefillTime) * refillRate
currentTokens = min(capacity, currentTokens + tokensToAdd)
lastRefillTime = now

Step 3: Allow/Reject
if (currentTokens >= 1) {
    currentTokens--
    return ALLOW
} else {
    return REJECT
}
```

### **Time-based Calculation**
```java
long timeSinceLastRefill = currentTime - lastRefillTime;
double tokensToAdd = timeSinceLastRefill * (refillRate / 1000);
currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
lastRefillTime = currentTime;
```

### **Formula**
```
tokens = min(capacity, tokens + (now - lastRefillTime) * refillRate)
```

---

## 🧱 Core Components

### 1. **RateLimiter Interface**
```java
interface RateLimiter {
    boolean allowRequest(String identifier);
    RateLimitResponse checkLimit(String identifier);
}

class RateLimitResponse {
    boolean allowed;
    int tokensRemaining;
    long resetTimeMs;
}
```

### 2. **Token Bucket Implementation**
```java
class TokenBucket {
    private double tokens;
    private final double capacity;
    private final double refillRate;  // tokens per millisecond
    private long lastRefillTime;
    
    synchronized boolean consumeToken() {
        refillTokens();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }
    
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastRefillTime;
        tokens = Math.min(capacity, 
                         tokens + (timePassed * refillRate / 1000.0));
        lastRefillTime = now;
    }
}
```

### 3. **User-based Rate Limiter**
```java
class UserBasedRateLimiter {
    private Map<String, TokenBucket> userBuckets;
    private final double capacity;
    private final double refillRate;
    
    boolean allowRequest(String userId) {
        userBuckets.computeIfAbsent(userId, 
            k → new TokenBucket(capacity, refillRate));
        return userBuckets.get(userId).consumeToken();
    }
}
```

---

## 🌐 Distributed Rate Limiting

### **Problem in Distributed Systems**
```
Multiple servers → Multiple rate limiters
User hits Server 1 (9/10 requests used)
User hits Server 2 (9/10 requests used)
User hits Server 1 again (not aware of Server 2)

Result: User exceeds limit! ❌
```

### **Solution: Redis-based Rate Limiter**

```
All servers query shared Redis:
┌────────────┐
│  Server 1  │
└──────┬─────┘
       │
┌──────▼─────────────────────┐
│      Redis (Central)        │
│  userId:tokenbucket:tokens │
│  userId:tokenbucket:lastTime│
└──────┬─────────────────────┘
       │
┌──────▼─────┐
│  Server 2  │
└────────────┘
```

### **Redis Implementation**
```java
class RedisRateLimiter {
    private RedisClient redis;
    
    boolean allowRequest(String userId, int capacity, int refillRate) {
        String key = "ratelimit:" + userId;
        
        // Atomic operation using Lua script
        String luaScript = """
            local tokens = redis.call('GET', KEYS[1])
            tokens = tonumber(tokens) or capacity
            
            local now = tonumber(ARGV[1])
            local lastRefill = tonumber(redis.call('GET', KEYS[2])) or now
            
            local timePassed = now - lastRefill
            tokens = math.min(capacity, tokens + (timePassed * refillRate / 1000))
            
            if tokens >= 1 then
                tokens = tokens - 1
                redis.call('SET', KEYS[1], tokens)
                redis.call('SET', KEYS[2], now)
                return 1
            else
                return 0
            end
        """;
        
        Long result = redis.eval(luaScript, 
            List.of(key + ":tokens", key + ":lastRefill"),
            List.of(System.currentTimeMillis(), capacity, refillRate));
        
        return result == 1;
    }
}
```

### **Why Lua Script?**
- Atomic operation on Redis
- No race conditions
- Prevents double-counting

---

## 🧵 Thread Safety (CRITICAL)

### **Problem**
```
Thread-1: tokensRemaining = 5
Thread-2: tokensRemaining = 5
Both check tokensRemaining >= 1 → true
Both proceed → Over-allowing!
```

### **Solutions**

#### 1. **Synchronized Method**
```java
synchronized boolean allowRequest() {
    refillTokens();
    if (tokens >= 1) {
        tokens--;
        return true;
    }
    return false;
}
```

#### 2. **ReentrantLock**
```java
private ReentrantLock lock = new ReentrantLock();

boolean allowRequest() {
    lock.lock();
    try {
        refillTokens();
        if (tokens >= 1) {
            tokens--;
            return true;
        }
        return false;
    } finally {
        lock.unlock();
    }
}
```

#### 3. **AtomicReference** (For simple cases)
```java
private AtomicReference<TokenBucket> bucket;

boolean allowRequest() {
    // Use compare-and-swap for atomic updates
    TokenBucket current = bucket.get();
    return current.consumeToken();
}
```

---

## 📊 Complexity Analysis

### **Token Bucket**
```
Time Complexity: O(1)
Space Complexity: O(u) where u = number of users
```

### **Sliding Window Log**
```
Time Complexity: O(n) where n = requests in window
Space Complexity: O(n)
```

### **Sliding Window Counter**
```
Time Complexity: O(1)
Space Complexity: O(m) where m = number of buckets
```

---

## ⚠️ Edge Cases

1. **Burst Requests** 📊
   - Token bucket allows controlled bursts
   - Subsequent requests denied appropriately

2. **Clock Skew** ⏰
   - Different servers have slightly different time
   - Use NTP synchronization
   - Redis stores time centrally

3. **Consumer Rebalancing** 🔄
   - When workers scale, tokens might reset
   - Design for idempotency
   - Store bucket state in Redis

4. **Thundering Herd** 🐘
   - Many users hitting limit simultaneously
   - Use queuing instead of rejection
   - Return 429 with Retry-After header

---

## 💡 Real-World Applications

- 🔐 **API Rate Limiting**: GitHub API (60 req/hr for anonymous)
- 🛡️ **DDoS Prevention**: Cloud providers limit requests
- 🔑 **Authentication**: Login attempts limit (5 per minute)
- 📤 **Upload Limits**: File upload sizes and frequency
- 💳 **Payment Processing**: Transaction frequency limits
- 📧 **Email Services**: Send limits to prevent spam

---

## 🧾 Interview Talking Points

### **Opening Statement** 🎤
> "I designed a rate limiter using the Token Bucket algorithm for optimal API rate limiting. The bucket has fixed capacity and refills at a constant rate. When a request arrives, I calculate tokens to add since last refill, then check if tokens are available. This achieves O(1) time complexity. For distributed systems, I use Redis with atomic Lua scripts to ensure consistency across multiple servers without race conditions."

### **Key Points to Emphasize**
✅ Token Bucket allows burst traffic (vs Sliding Window)  
✅ O(1) decision making even under high concurrency  
✅ Redis for distributed consistency  
✅ Lua scripts for atomic operations  
✅ Thread safety with locks/synchronized  
✅ Trade-offs between different algorithms  

### **Follow-up Questions to Expect**
1. **"Why Token Bucket over Sliding Window?"**
   - O(1) vs O(n)
   - Burst handling
   - Memory efficiency

2. **"How to handle distributed systems?"**
   - Redis central store
   - Atomic Lua operations
   - No race conditions

3. **"What if Redis is down?"**
   - Local fallback with reduced capacity
   - Pessimistic approach (reject more)
   - Queue and retry logic

4. **"How to handle clock skew?"**
   - Use server-based time (Redis time)
   - NTP synchronization
   - Graceful degradation

---

## 📂 Project Structure

```
RateLimiter/
├── src/
│   ├── interfaces/
│   │   ├── RateLimiter.java
│   │   └── RateLimitResponse.java
│   ├── algorithms/
│   │   ├── TokenBucketRateLimiter.java
│   │   ├── SlidingWindowLogRateLimiter.java
│   │   ├── SlidingWindowCounterRateLimiter.java
│   │   └── LeakyBucketRateLimiter.java
│   ├── distributed/
│   │   ├── RedisRateLimiter.java
│   │   └── RedisTokenBucket.java
│   ├── concurrent/
│   │   ├── ThreadSafeTokenBucket.java
│   │   └── ConcurrentRateLimiter.java
│   ├── demo/
│   │   └── RateLimiterDemo.java
│   └── test/
│       └── RateLimiterTest.java
├── README.md (this file)
├── notes.md
└── [Additional code will be added]
```

---

## 🔧 Getting Started

### Prerequisites
- Java 8+
- Redis (for distributed version)
- Understanding of:
  - Concurrency
  - Time-based algorithms
  - Distributed systems

### Running Demo
```bash
cd RateLimiter
javac -d bin src/**/*.java
java -cp bin demo.RateLimiterDemo
```

---

## 🏁 Summary

The Rate Limiter demonstrates:
- Algorithm selection and trade-offs
- O(1) optimization techniques
- Distributed system design
- Thread-safety and concurrency
- Real-world system constraints

**Time to Implement**: ~3-4 hours  
**Difficulty**: ⭐⭐⭐⭐☆ (Hard)  
**Interview Frequency**: ⭐⭐⭐⭐☆ (Common in system design)

**Next Steps**: Review `demo/RateLimiterDemo.java` for practical usage examples and test different algorithms.
