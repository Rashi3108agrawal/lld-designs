# 🧠 LRU Cache (Least Recently Used) – Complete Interview Notes

---

## 📌 Overview

Design and implement a **Least Recently Used (LRU) Cache** that supports:

- `get(key)` → returns value if present, else -1  
- `put(key, value)` → inserts or updates value  
- When capacity is full → evict **Least Recently Used (LRU)** item  

### 🎯 Core Requirement
All operations must run in:

> ⚡ O(1) Time Complexity

This is a very common interview question that tests:
- Data structure knowledge
- Optimization ability
- System design thinking

---

## 🧱 Key Idea / Intuition

We need:
- Fast lookup → HashMap
- Fast ordering + deletion → Doubly Linked List

So we combine both.

---

## 🔥 Core Design Idea

### We maintain:
- HashMap → key → Node
- Doubly Linked List → usage order

### Order rule:
- Head → Most Recently Used (MRU)
- Tail → Least Recently Used (LRU)

---

## 🧩 Why Two Data Structures?

| Requirement | Data Structure |
|------------|----------------|
| Fast access | HashMap |
| Fast deletion & ordering | Doubly Linked List |

Neither alone can achieve O(1) for both operations.

---

## 🔄 Working Logic

### GET Operation
1. Check HashMap
2. If not found → return -1
3. If found:
   - Move node to head (MRU)
   - Return value

---

### PUT Operation
1. If key exists:
   - Update value
   - Move node to head
2. Else:
   - If capacity full:
     - Remove tail (LRU)
     - Delete from HashMap
   - Insert new node at head
   - Add to HashMap

---

## 🧱 Data Structures Used

### 1. HashMap
- Stores: key → Node
- O(1) access

### 2. Doubly Linked List
- Stores usage order
- O(1) insertion/deletion

---

## 📍 Node Structure

Each node contains:
- key
- value
- prev pointer
- next pointer

---

## 🧠 Design Patterns Used

### 🔥 1. Composite Pattern (CORE)
- Combination of:
  - HashMap
  - Doubly Linked List

This is the **actual core design pattern**

---

### 🔥 2. Strategy Pattern (EXTENSION)
Used if we support multiple eviction policies:
- LRU (current)
- LFU
- FIFO

---

### 🔥 3. Singleton Pattern (OPTIONAL)
Used if cache is global:
- One instance per application

---

### 🔥 4. Factory Pattern (OPTIONAL)
Used for creating cache instances dynamically:
- LRUCache
- LFUCache

---

## 🧱 SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)
- Node → stores data only
- Cache → handles logic
- LinkedList → handles ordering

---

### 2. Open/Closed Principle (OCP)
- Can extend eviction strategies without modifying core logic

---

### 3. Liskov Substitution Principle (LSP)
- Different eviction strategies can replace each other

---

### 4. Interface Segregation Principle (ISP)
- Separation between cache logic and eviction logic

---

### 5. Dependency Inversion Principle (DIP)
- Cache depends on abstraction (strategy), not concrete logic

---

## ⚙️ Advantages

- ✔ O(1) get operation
- ✔ O(1) put operation
- ✔ Automatic eviction
- ✔ Memory efficient (bounded size)

---

## 📊 Complexity Analysis

| Operation | Time |
|----------|------|
| get()    | O(1) |
| put()    | O(1) |

### Space Complexity:
- O(capacity)

---

## 💡 When to Use LRU Cache

- Browser cache
- Database query cache
- Image caching in apps
- Operating system memory management
- CDN caching systems

---

## 🧠 Real-World Intuition

Systems prefer keeping:
> “Most recently used data is more likely to be used again”

So old unused data is removed automatically.

---

## 🔥 Best Practices

- Use dummy head and tail nodes
- Always move accessed node to head
- Handle capacity edge cases
- Keep operations atomic (important for thread safety)
- Maintain consistency between HashMap and DLL

---

## ⚠️ Common Mistakes

- Using only LinkedList → O(n) operations ❌
- Using only HashMap → no ordering ❌
- Not updating order on get ❌
- Forgetting eviction step ❌

---

## 🧵 Thread Safety (Important Extension)

For multi-threaded systems:
- Use synchronized blocks OR
- ReentrantLock OR
- ConcurrentHashMap + locking strategy

---

## 🧠 Interview Explanation Script

> “I used a HashMap for O(1) access and a doubly linked list to maintain usage order. Every time a key is accessed, I move it to the front as most recently used. When capacity is exceeded, I remove the node at the tail which represents the least recently used element.”

---

## 🏁 Summary

LRU Cache is a classic system design + data structure problem that demonstrates:

- Strong understanding of HashMap + LinkedList
- Ability to optimize for O(1)
- Real-world caching behavior
- Extensibility using design patterns

It is widely used in:
- Operating systems
- Web browsers
- Distributed caching systems
- Backend systems

---