# 🧠 LRU Cache – Low Level Design

A **Least Recently Used (LRU) Cache** implementation demonstrating **O(1) time complexity** for all operations. This is a classic system design problem that tests deep understanding of data structures and optimization.

---

## 📌 Problem Statement

Design and implement an **LRU Cache** that supports:

- ✅ `get(key)` → returns value if present, else -1
- ✅ `put(key, value)` → inserts or updates value
- ✅ When capacity is full → evict **Least Recently Used (LRU)** item
- ✅ All operations in **O(1) time complexity**

### 🎯 Core Challenge

Achieve **O(1)** for all operations (get, put, evict). This requires clever combination of data structures!

---

## 🧱 Key Idea / Intuition

We need to answer two questions in O(1):
1. **"Is this key in cache?"** → HashMap
2. **"Which item is least recently used?"** → Doubly Linked List

**We can't use just one!** We need to combine both.

---

## 🔥 Design Approach

### Why Two Data Structures?

| Requirement | Problem | Solution |
|-------------|---------|----------|
| Fast key lookup | O(n) with list | HashMap → O(1) |
| Maintain usage order | O(n) with list | Doubly Linked List → O(1) |
| Fast deletion from middle | O(n) with array | Doubly Linked List → O(1) |

---

## 🧩 Core Data Structures

### 1. **HashMap**
```
Key → Node Reference
Purpose: O(1) access to any node

Structure:
Map<Integer, Node> cache = {
    1 → Node(key=1, value=10),
    2 → Node(key=2, value=20),
    3 → Node(key=3, value=30)
}
```

### 2. **Doubly Linked List**
```
Maintains usage order:
Head ← (MRU) ← Node ← Node ← ... ← Node ← (LRU) → Tail

Properties:
- Head: Most Recently Used item
- Tail: Least Recently Used item
- Each node has prev/next pointers
```

### 3. **Node Structure**
```java
class Node {
    int key;
    int value;
    Node prev;     // pointer to previous node
    Node next;     // pointer to next node
}
```

---

## 🔄 How It Works

### **GET Operation**

```
get(key):
1. Check if key exists in HashMap
   ├─ If NOT found → return -1
   └─ If found:
      ├─ Move node to head (mark as MRU)
      └─ Return value
```

**Example**: get(2) in [1→2→3] becomes [2→1→3]

**Time**: O(1) - HashMap lookup + list pointer update

---

### **PUT Operation**

```
put(key, value):
1. If key already exists:
   ├─ Update value
   └─ Move node to head (mark as MRU)
   
2. Else (new key):
   ├─ If cache is full:
   │  ├─ Delete tail node (LRU item)
   │  └─ Remove from HashMap
   │
   ├─ Create new node
   ├─ Insert at head
   └─ Add to HashMap
```

**Example**: put(4, 40) in full cache [1→2→3]
- Result: [4→1→2] (3 evicted as LRU)

**Time**: O(1) - All operations are constant time

---

## 📊 Visual Examples

### Initial State (Capacity = 3)
```
Cache: Empty
DLL: Head ←→ Tail
HashMap: {}
```

### After put(1, 10)
```
Cache: 1
DLL: Head ←→ [1] ←→ Tail
HashMap: {1 → Node[1]}
```

### After put(2, 20), put(3, 30)
```
Cache: 3 → 2 → 1 (Head to Tail, MRU to LRU)
DLL: Head ←→ [3] ←→ [2] ←→ [1] ←→ Tail
HashMap: {1→Node[1], 2→Node[2], 3→Node[3]}
```

### After get(1) → returns 10
```
Cache: 1 → 3 → 2 (1 moved to front as MRU)
DLL: Head ←→ [1] ←→ [3] ←→ [2] ←→ Tail
```

### After put(4, 40) → Capacity Exceeded
```
Cache: 4 → 1 → 3 (2 evicted as LRU)
DLL: Head ←→ [4] ←→ [1] ←→ [3] ←→ Tail
HashMap: {1→Node[1], 3→Node[3], 4→Node[4]}
Note: 2 was at tail (LRU), so it was removed
```

---

## 🧠 Design Patterns Used

### 1. **Composite Pattern** (Core)
Combination of:
- HashMap (for fast lookup)
- Doubly Linked List (for ordering)

This composite structure enables O(1) for all operations.

### 2. **Strategy Pattern** (Extension)
Support multiple eviction policies:
- **LRU** (current): Evict least recently used
- **LFU** (future): Evict least frequently used
- **FIFO** (future): Evict first in first out

```java
interface EvictionStrategy {
    Node selectVictim();
}
```

### 3. **Singleton Pattern** (Optional)
If cache is application-wide:
```java
class CacheManager {
    private static LRUCache instance;
    public static LRUCache getInstance() { ... }
}
```

---

## ✨ SOLID Principles Applied

### **Single Responsibility Principle (SRP)**
- `Node`: Stores data and pointers only
- `LRUCache`: Manages cache logic
- `LinkedList`: Manages ordering only

### **Open/Closed Principle (OCP)**
- Can add new eviction strategies without changing LRUCache
- Core logic remains closed to modification

### **Liskov Substitution Principle (LSP)**
- Different eviction strategies can replace each other
- Users interact through common interface

### **Interface Segregation Principle (ISP)**
- Clear separation between cache and eviction logic
- Each interface has minimal, focused methods

### **Dependency Inversion Principle (DIP)**
- LRUCache depends on EvictionStrategy interface
- Not on concrete implementations

---

## 📊 Complexity Analysis

| Operation | Time | Space |
|-----------|------|-------|
| `get(key)` | O(1) | - |
| `put(key, value)` | O(1) | - |
| **Overall Space** | - | O(capacity) |

### Why O(1)?
- HashMap operations: O(1) average
- DLL node insertion/deletion: O(1) (with pointer)
- No loops or recursion

---

## ⚠️ Common Mistakes

❌ **Using only LinkedList**
- get(key): O(n) - have to search linearly

❌ **Using only HashMap**
- No ordering information
- Can't identify LRU item

❌ **Not updating order on get()**
- Cache doesn't reflect actual usage
- Wrong items get evicted

❌ **Not removing from HashMap during eviction**
- Memory leak - orphaned nodes accumulate

❌ **Using singly linked list**
- Deleting middle node requires O(n) predecessor search
- Need prev pointers for O(1) deletion

---

## 🧵 Thread Safety (Important Extension)

For multi-threaded systems:

### Problem
```
Thread-1: get(key1)
Thread-2: put(key2, value2)
Race condition: Inconsistent state
```

### Solution Options

#### 1. **Synchronized Block**
```java
synchronized (cache) {
    // Critical section
}
```
Simple but can cause contention.

#### 2. **ReentrantReadWriteLock**
```java
ReadWriteLock lock = new ReentrantReadWriteLock();
// Multiple readers or single writer
```
Better for read-heavy workloads.

#### 3. **ConcurrentHashMap + Fine-grained Locks**
```java
ConcurrentHashMap<Integer, Node> cache;
ReentrantLock nodeLock;
```
Better performance for high concurrency.

---

## 💡 Real-World Applications

This pattern is used in:
- 🌐 **Web Browsers**: Cache recently visited pages
- 🗄️ **Databases**: Buffer pools for frequently accessed data
- 📱 **Mobile Apps**: Image caching in Instagram, Facebook
- 💾 **Operating Systems**: Page replacement in virtual memory
- 🚀 **CDNs**: Edge cache management
- 🔍 **Search Engines**: Query result caching

---

## 🔧 Getting Started

### Prerequisites
- Java 8+
- Understanding of:
  - HashMap/HashTable
  - Linked Lists
  - Basic design patterns

### Implementation
```bash
cd LRUCache
javac -d bin src/**/*.java
java -cp bin demo.LRUCacheDemo
```

---

## 📝 Usage Example

```java
// Create cache with capacity 3
LRUCache cache = new LRUCache(3);

// Put values
cache.put(1, 10);  // {1=10}
cache.put(2, 20);  // {1=10, 2=20}
cache.put(3, 30);  // {1=10, 2=20, 3=30}

// Get values (updates recency)
cache.get(1);      // Returns 10, {1=10} moved to head
// Now order: 1(MRU) → 2 → 3(LRU)

// Put evicts LRU
cache.put(4, 40);  // {1=10, 4=40, 2=20}
// 3 was LRU, so it was evicted
// Now order: 4(MRU) → 1 → 2(LRU)

// Get non-existent
cache.get(3);      // Returns -1 (3 was evicted)
```

---

## 🧾 Interview Talking Points

### **Opening Statement** 🎤
> "I implemented an LRU Cache using a HashMap for O(1) key lookup and a doubly linked list to maintain usage order. The head represents the most recently used item, and the tail represents the least recently used item. Every access or update moves the item to the head, and when capacity is exceeded, I evict the tail node. This design achieves O(1) for all operations."

### **Key Points to Emphasize**
✅ Why two data structures are necessary  
✅ How O(1) is achieved for all operations  
✅ Why doubly linked list is needed (not singly)  
✅ How eviction works and why tail is chosen  
✅ Memory management and preventing leaks  

### **Follow-up Questions to Expect**
1. **"Why not use a TreeMap for ordering?"**
   - TreeMap: O(log n) operations
   - DLL: O(1) operations
   - Trade-off: Space for speed

2. **"How would you make it thread-safe?"**
   - Synchronized blocks
   - ReadWriteLock for read-heavy workloads
   - Fine-grained locking with ConcurrentHashMap

3. **"How to support LFU instead of LRU?"**
   - Track frequency of access
   - Evict least frequently used
   - Use PriorityQueue for frequency ordering

4. **"What if capacity changes at runtime?"**
   - Adjust capacity field
   - Evict extra items if new capacity < current size
   - O(n) operation for eviction

---

## 📂 Project Structure

```
LRUCache/
├── src/
│   ├── cache/
│   │   ├── LRUCache.java         # Main implementation
│   │   ├── Node.java              # Doubly linked list node
│   │   └── EvictionPolicy.java    # Interface for strategies
│   ├── strategies/
│   │   ├── LRUEviction.java
│   │   ├── LFUEviction.java       # Future: Least frequently used
│   │   └── FIFOEviction.java      # Future: First in first out
│   ├── concurrent/
│   │   └── ConcurrentLRUCache.java # Thread-safe version
│   ├── demo/
│   │   └── LRUCacheDemo.java
│   └── test/
│       └── LRUCacheTest.java
├── README.md (this file)
├── notes.md
└── [Additional code will be added]
```

---

## 🎯 Key Learnings

After studying this implementation, you'll understand:

1. ✅ How to combine data structures for optimal complexity
2. ✅ Trade-offs between time and space complexity
3. ✅ Importance of pointer manipulation in linked lists
4. ✅ How to design eviction strategies
5. ✅ Thread-safety considerations in caching
6. ✅ Real-world caching patterns

---

## 🏁 Summary

The LRU Cache demonstrates:
- **Optimization skills**: Achieving O(1) for all operations
- **Data structure knowledge**: HashMap + Doubly Linked List
- **Problem-solving**: Trading space for time
- **Design patterns**: Strategy pattern for extensibility
- **System design**: Real-world caching behavior

**This is a highly popular interview question that shows you understand optimization at a deep level!**

---

**Time to Implement**: ~2-3 hours for full solution  
**Difficulty**: ⭐⭐⭐⭐☆ (Hard)  
**Interview Frequency**: ⭐⭐⭐⭐⭐ (Very Common)

**Next Steps**: Review `demo/LRUCacheDemo.java` for practical usage examples and test cases.
