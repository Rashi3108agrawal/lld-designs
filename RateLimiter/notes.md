# 🚦 Rate Limiter System – Low Level Design (LLD)

---

## 📌 Overview

Design a **Rate Limiter** for an API endpoint to prevent abuse, throttling violations, and DDoS attacks.

The system should:

- Allow or deny incoming requests
- Enforce per-user / per-IP limits
- Work in distributed environments
- Support high concurrency (O(1) decision per request)

---

## 🎯 Core Requirements

- Limit number of requests per user/IP
- Allow burst traffic (optional depending on algorithm)
- Ensure low latency (O(1) per request)
- Work in distributed systems (multiple servers)
- Be thread-safe under high concurrency

---

## 🧱 Key Design Goals

- ⚡ Fast decision making (O(1))
- 🌐 Distributed support (Redis / cache)
- 🧵 Thread-safe under heavy load
- 📊 Accurate or approximate fairness depending on algorithm

---

# ⚙️ Core Algorithms

---

## 1. 🪣 Token Bucket Algorithm (MOST IMPORTANT)

### 🔥 Idea

- Bucket has fixed capacity (max tokens)
- Tokens refill at fixed rate
- Each request consumes 1 token
- If no tokens → request denied

---

### 📌 Behavior

| State | Result |
|------|--------|
| Token available | Allow request |
| No token | Reject request |

---

### 📊 Properties

- Allows burst traffic ✔
- Smooth rate limiting ✔
- Simple O(1) logic ✔

---

### 🧠 Formula
requests in last window =
current bucket + previous bucket weighted


---

### 🟢 Pros
- Memory efficient
- O(1) operations

### 🔴 Cons
- Approximation errors

---

# 4. 🪣 Leaky Bucket Algorithm

### 🔥 Idea

- Requests enter queue
- Processed at constant rate

---

### 📌 Behavior

- Smooth output rate
- Excess requests dropped

---

### 🟢 Pros
- Smooth traffic shaping

### 🔴 Cons
- No burst handling

---

# 🧠 Token Bucket vs Sliding Window (IMPORTANT INTERVIEW SECTION)

| Feature | Token Bucket | Sliding Window |
|--------|-------------|----------------|
| Burst handling | ✅ Yes | ❌ No |
| Accuracy | Medium | High |
| Memory | O(1) | O(n) |
| Performance | Very fast | Slower |
| Use case | APIs, gateways | Billing, strict limits |

---

## 🎯 Conclusion:

> Token Bucket is preferred for APIs  
> Sliding Window is preferred for strict fairness

---

# 🧱 System Design (High Level)
Client
↓
API Gateway / Rate Limiter Middleware
↓
Redis / In-memory store
↓
Decision Engine (ALLOW / REJECT)


---

# ⚙️ Core Components

## 1. RateLimiter Interface

- checkLimit(userId)

---

## 2. Token Bucket Store

- capacity
- tokens
- lastRefillTime

---

## 3. Redis (Distributed Mode)

- userId → token state

---

## 4. Sync Layer (Concurrency)

- synchronized / locks
- or atomic Redis operations

---

# 🧠 Thread Safety (CRITICAL)

## Problems:
- multiple requests update same bucket
- race condition → over-allowing requests

---

## Solutions:

### ✔ In-memory
- synchronized block
- ReentrantLock

### ✔ Distributed
- Redis atomic Lua script
- INCR + EXPIRE patterns

---
# 🚦 Rate Limiter – Quick Revision Add-On (Important Concepts)

---

## 🧠 Core Idea
Rate limiter controls number of requests per user/IP in a time window to prevent:
- DDoS attacks
- API abuse
- system overload

---

# ⚙️ Algorithms (Quick Recall)

## 🪣 1. Token Bucket (MOST IMPORTANT)
- Bucket has fixed capacity
- Tokens refill over time
- Each request consumes 1 token

### Formula:
tokens = min(capacity, tokens + time * refill_rate)

### Key Points:
- Allows burst traffic
- O(1) decision time
- Best for APIs

---

## 🪟 2. Sliding Window Log
- Store timestamps of all requests
- Remove old entries outside window
- Count remaining requests

### Key Points:
- Very accurate
- High memory (O(n))
- Slow under load

---

## 📊 3. Sliding Window Counter
- Divide time into buckets (1 sec intervals)
- Maintain count per bucket
- Sum recent buckets

### Key Points:
- O(1) performance
- Approximate result
- Good balance of speed + memory

---

## 🪣 4. Leaky Bucket
- Requests go into queue
- Processed at constant rate
- Overflow → drop requests

### Key Points:
- Smooth traffic
- No bursts allowed
- Good for shaping traffic

---

# ⚖️ Token Bucket vs Sliding Window

| Feature | Token Bucket | Sliding Window |
|--------|-------------|---------------|
| Burst support | ✅ Yes | ❌ No |
| Accuracy | Medium | High |
| Memory | O(1) | O(n) |
| Use case | APIs | Billing / strict limits |

---

# 🌐 Distributed Rate Limiting

## Problem:
Multiple servers → inconsistent limits

## Solution:
Use Redis:
- userId → token state
- atomic updates via Lua scripts

---

# 🧵 Concurrency (VERY IMPORTANT)

## Problem:
Multiple threads modifying same bucket

## Fix:
### In-memory:
- synchronized
- ReentrantLock

### Distributed:
- Redis atomic operations

---

# ⚡ Complexity
- Allow/Reject check → O(1)

---

# 🧠 Interview Must-Say Points

- Token Bucket is best for APIs
- Sliding Window is most accurate
- Redis is needed for distributed systems
- Concurrency is critical issue
- O(1) decision is required

---

# 💡 Interview Script (Short)

“Rate limiter controls request flow using Token Bucket algorithm where tokens are refilled over time and consumed per request. It allows burst traffic while enforcing long-term rate limits. In distributed systems, we use Redis for shared state and atomic updates to ensure consistency across multiple servers.”

---

# 🚀 Key Takeaway
👉 Token Bucket = API systems  
👉 Sliding Window = strict fairness  
👉 Redis = distributed control  
👉 Locks = thread safety