# 🚗 Parking Lot System – Low Level Design

A **thread-safe, scalable Parking Lot Management System** built with clean architecture principles and design patterns. This system demonstrates real-world LLD concepts used in interview rounds.

---

## 📌 Problem Statement

Design a **Parking Lot System** that supports:

- ✅ Multiple parking floors with flexible spot allocation
- ✅ Multiple parking spot types (Compact, Large, Handicapped, Motorcycle)
- ✅ Multiple vehicle types (Car, Truck, Motorcycle, Van)
- ✅ Parking and unparking operations
- ✅ Automatic ticket generation on entry
- ✅ Dynamic fee calculation based on parking duration
- ✅ Thread-safe concurrent access for multiple vehicles
- ✅ Extensible pricing strategies

---

## 🎯 Core Requirements

### Functional Requirements
1. Find and allocate available parking spots
2. Generate tickets on entry with entry time
3. Calculate fees on exit based on parking duration
4. Support multiple pricing strategies
5. Track vehicle-to-spot mappings
6. Handle vehicle unparking and spot release

### Non-Functional Requirements
1. **Concurrency**: Handle multiple simultaneous parking/unparking requests
2. **Performance**: Fast spot search and allocation
3. **Scalability**: Support thousands of spots across multiple floors
4. **Reliability**: No race conditions, atomic operations
5. **Extensibility**: Easy to add new vehicle types and pricing strategies

---

## 🧱 Core Entities

### 1. **ParkingLot** (Singleton)
- System controller and entry point
- Manages floors and overall operations
- Thread-safe using double-checked locking

```
Properties:
- floors: List<ParkingFloor>
- activeTickets: ConcurrentHashMap<String, Ticket>
- vehicleToSpotMapping: ConcurrentHashMap<String, ParkingSpot>
- pricingStrategy: PricingStrategy
```

### 2. **ParkingFloor**
- Container for parking spots
- Handles floor-level spot allocation

```
Properties:
- floorNumber: int
- spots: List<ParkingSpot>
- lock: ReentrantLock (for thread safety)
```

### 3. **ParkingSpot**
- Individual parking slot
- Can be empty or occupied by a vehicle

```
Properties:
- spotNumber: String
- spotType: SpotType (COMPACT, LARGE, HANDICAPPED, MOTORCYCLE)
- vehicle: Vehicle (nullable)
- isAvailable: boolean
- lock: ReentrantLock
```

### 4. **Vehicle** (Abstract)
- Base class for all vehicle types
- Defines vehicle dimensions and requirements

```
Subclasses:
- Car
- Truck
- Motorcycle
- Van

Properties:
- licensePlate: String
- vehicleType: VehicleType
- dimensions: Dimensions
```

### 5. **Ticket**
- Proof of parking entry
- Contains vehicle and temporal data

```
Properties:
- ticketId: String
- licensePlate: String
- entryTime: long
- exitTime: long
- parkingSpot: ParkingSpot
- status: TicketStatus (ACTIVE, COMPLETED)
```

### 6. **PricingStrategy** (Interface)
- Calculates parking fees
- Supports multiple pricing algorithms

```
Implementations:
- HourlyPricingStrategy
- FixedPricingStrategy
- DynamicPricingStrategy
```

---

## 🔗 Relationships

| Relationship | Type | Description |
|-------------|------|-------------|
| ParkingLot → ParkingFloor | HAS-A (Composition) | Parking lot contains multiple floors |
| ParkingFloor → ParkingSpot | HAS-A (Composition) | Floor contains multiple spots |
| ParkingSpot ↔ Vehicle | ASSOCIATES | Spot holds one vehicle at a time |
| Ticket ↔ Vehicle | REFERENCES | Ticket tracks vehicle entry/exit |
| ParkingLot → PricingStrategy | USES (Dependency Injection) | Lot uses strategy for calculations |

---

## 🔄 System Flow

### **Parking Flow** 🚙

```
1. User requests parking for Vehicle
   ↓
2. ParkingLot.parkVehicle(vehicle) called
   ↓
3. Find suitable floor:
   - Check each floor for availability
   - Acquire floor lock for thread safety
   ↓
4. Find suitable spot:
   - Find first available spot matching vehicle type
   - Acquire spot lock
   ↓
5. Create & store Ticket:
   - Generate unique ticketId
   - Record entry time
   - Store in activeTickets map
   ↓
6. Create mappings:
   - vehicle → spot mapping
   - ticket → vehicle mapping
   ↓
7. Return Ticket to user
```

### **Unparking Flow** 🚗

```
1. User provides Ticket at exit
   ↓
2. Validate ticket exists
   ↓
3. Retrieve vehicle and spot from mappings
   ↓
4. Calculate fee:
   - Duration = exitTime - entryTime
   - Fee = pricingStrategy.calculateFee(vehicle, duration)
   ↓
5. Release spot:
   - Mark spot as available
   - Clear vehicle reference
   - Release lock
   ↓
6. Remove mappings:
   - Delete from activeTickets
   - Delete from vehicleToSpot map
   ↓
7. Return final price to user
```

---

## 🧵 Concurrency & Thread Safety

### Why Thread Safety is Critical

In real-world systems, hundreds of vehicles may try to park/unpark simultaneously. Without proper synchronization:
- **Race Condition**: Two vehicles might get assigned the same spot
- **Data Corruption**: Mappings become inconsistent
- **Lost Updates**: Ticket information might be lost

### Solutions Implemented

#### 1. **ReentrantLock** (Per-Spot & Per-Floor)
- Ensures only one thread can access a spot at a time
- Prevents double-booking
- Allows same thread to acquire lock multiple times (reentrant)

```java
// Lock protects critical section
spotLock.lock();
try {
    if (spot.isAvailable()) {
        spot.assignVehicle(vehicle);
        // Atomic operation
    }
} finally {
    spotLock.unlock();
}
```

#### 2. **ConcurrentHashMap**
- Thread-safe hash map for:
  - `activeTickets`: Manages concurrent ticket storage
  - `vehicleToSpotMapping`: Tracks vehicle positions safely
- O(1) average operations with built-in synchronization

#### 3. **Singleton Pattern** (Double-Checked Locking)
- Ensures only one ParkingLot instance across the application
- Lazy initialization with minimal overhead

```java
if (instance == null) {
    synchronized (ParkingLot.class) {
        if (instance == null) {
            instance = new ParkingLot();
        }
    }
}
```

---

## 🎯 Design Patterns Used

### 1. **Singleton Pattern**
**Purpose**: Ensure single ParkingLot instance globally

```
Benefits:
- Global access point
- Single source of truth for parking state
- Prevents multiple lot instances
```

### 2. **Strategy Pattern**
**Purpose**: Flexible pricing calculation

```
Usage:
- HourlyPricingStrategy: Fixed rate per hour
- FixedPricingStrategy: Flat rate regardless of duration
- DynamicPricingStrategy: Variable rates based on demand

Benefits:
- Add new pricing without modifying existing code
- Runtime algorithm selection
- Easy testing with mock strategies
```

### 3. **Composition Pattern**
**Purpose**: Build hierarchy (Lot → Floors → Spots)

```
Benefits:
- Natural representation of real-world structure
- Encapsulation of responsibilities
- Scalability to handle large parking lots
```

### 4. **Polymorphism**
**Purpose**: Handle different vehicle types uniformly

```
Example:
- Vehicle.getRequiredSpotType() implemented differently for each subclass
- All vehicles processed through same interface
- Easy to add new vehicle types
```

---

## ✨ OOP Principles Applied

### **Encapsulation** 🔒
- Private state in ParkingSpot (vehicle, availability)
- Public only necessary methods (park, unpark, available)
- Internal state protected from unauthorized access

### **Abstraction** 🎭
- Vehicle: Abstract class hiding implementation details
- PricingStrategy: Interface abstracting pricing logic
- Users interact with abstractions, not concrete classes

### **Inheritance** 🏗️
- Vehicle hierarchy (Car, Truck, Motorcycle)
- Specific vehicle types extend base Vehicle
- Code reuse across vehicle types

### **Polymorphism** 🔄
- Vehicle subclasses override abstract methods
- Pricing strategies implement common interface
- Same operation (getRequiredSpotType) behaves differently per vehicle type

---

## ⚠️ Edge Cases Handled

1. **No Available Spot** ❌
   - Returns error/exception
   - Vehicle not parked
   - User notified

2. **Concurrent Parking Requests** 🔀
   - Lock ensures only one succeeds
   - Others wait or fail gracefully
   - No double-booking

3. **Invalid Unpark Attempt** ❌
   - Vehicle not found in system
   - Ticket not found or expired
   - Exception thrown

4. **Double Unpark** 🔁
   - Ticket already processed
   - Spot already released
   - Prevent fraud/errors

5. **Spot Already Occupied** 🚫
   - Another vehicle already assigned
   - Recheck and try different spot
   - Graceful retry logic

6. **Pricing Edge Cases** 💰
   - Vehicle type not supported for spot
   - Duration < 1 minute (minimum charge)
   - Special pricing for extended stays

---

## ⏱️ Complexity Analysis

### **Parking Operation**
```
Time Complexity: O(F × S)
  F = number of floors
  S = spots per floor
  
Worst case: Search all floors and spots
Average case: O(1) with good spot distribution
```

### **Unparking Operation**
```
Time Complexity: O(1)
  HashMap lookup: O(1) average
  Spot release: O(1)
  Calculation: O(1)
```

### **Space Complexity**
```
O(F × S + V)
  F × S = total spots
  V = active vehicles
```

---

## 💰 Pricing Strategies

### **Implementation**
The system is extensible to support multiple pricing models:

```java
interface PricingStrategy {
    double calculateFee(Vehicle vehicle, long durationMillis);
}
```

### **Examples**

#### 1. **Hourly Pricing**
```
Car: $5 per hour
Motorcycle: $2 per hour
Truck: $10 per hour

Formula: durationHours × ratePerHour
```

#### 2. **Fixed Pricing**
```
Flat rate: $10 per vehicle
Regardless of duration
```

#### 3. **Dynamic Pricing** (Future Enhancement)
```
Base rate × demand multiplier
Higher rates during peak hours
Surge pricing for special events
```

---

## 🚀 Scalability Improvements (Discussion Points)

### Current System
- In-memory data structures
- Single-threaded coordination point
- Limited to single server

### Potential Enhancements

1. **Reservation System** 📅
   - Pre-book spots in advance
   - Reduce search time
   - Improve user experience

2. **Database Integration** 🗄️
   - Persistent storage
   - Historical analytics
   - Audit trails

3. **Payment Gateway** 💳
   - Online payment processing
   - Digital receipts
   - Multiple payment methods

4. **Multi-Location Support** 🌍
   - City-wide parking management
   - Cross-location availability
   - Centralized billing

5. **EV Charging Integration** ⚡
   - Special EV charging spots
   - Battery management alerts
   - Charging-based pricing

6. **Display Boards** 📊
   - Real-time availability updates
   - Digital signage
   - Mobile app integration

7. **Microservice Architecture** 🏛️
   - Separate services for:
     - Spot management
     - Pricing calculation
     - Payment processing
   - Horizontal scaling
   - Independent deployments

---

## 🧾 Interview Talking Points

### **Opening Statement** 🎤
> "I designed a thread-safe Parking Lot system using a Singleton for global access, Composition for building the lot hierarchy (Floors → Spots), and the Strategy Pattern for flexible pricing. The system handles high concurrency through ReentrantLocks and ConcurrentHashMaps to ensure no race conditions."

### **Key Points to Emphasize**
- ✅ Thread-safe design with proper synchronization
- ✅ Composition over inheritance for hierarchy
- ✅ Strategy pattern for extensible pricing
- ✅ Clear separation of concerns
- ✅ Real-world modeling with practical edge cases
- ✅ O(1) unpark operations despite O(F×S) park operations

### **Follow-up Questions to Expect**
1. **"How would you handle thousands of vehicles?"**
   - Distributed system with Redis
   - Database for persistence
   - Horizontal scaling of services

2. **"What if two threads try to park same spot?"**
   - ReentrantLock prevents this
   - Atomic check-and-set operation
   - Other thread waits or gets alternative spot

3. **"How to support reservations?"**
   - Add reservation state to spots
   - Modify search logic to skip reserved spots
   - Add cancellation and timeout logic

4. **"What about payment failures?"**
   - Add payment service layer
   - Retry logic with exponential backoff
   - Move to DLQ for manual review

---

## 📂 Project Structure

```
ParkingLotSystem/
├── src/
│   ├── entities/
│   │   ├── ParkingLot.java
│   │   ├── ParkingFloor.java
│   │   ├── ParkingSpot.java
│   │   ├── Vehicle.java
│   │   ├── Car.java
│   │   ├── Truck.java
│   │   ├── Motorcycle.java
│   │   ├── Van.java
│   │   ├── Ticket.java
│   │   └── enums/
│   │       ├── VehicleType.java
│   │       ├── SpotType.java
│   │       └── TicketStatus.java
│   ├── strategies/
│   │   ├── PricingStrategy.java
│   │   ├── HourlyPricingStrategy.java
│   │   ├── FixedPricingStrategy.java
│   │   └── DynamicPricingStrategy.java
│   ├── exceptions/
│   │   ├── ParkingException.java
│   │   ├── NoSpotAvailableException.java
│   │   ├── InvalidTicketException.java
│   │   └── InvalidVehicleException.java
│   └── demo/
│       └── ParkingLotDemo.java
├── README.md (this file)
├── notes.md
└── [Additional code will be added]
```

---

## 🔧 Getting Started

### Prerequisites
- Java 8+
- Maven or Gradle
- Basic understanding of:
  - Multithreading concepts
  - Design patterns
  - OOP principles

### Compilation
```bash
cd ParkingLotSystem
javac -d bin src/**/*.java
```

### Running Demo
```bash
java -cp bin demo.ParkingLotDemo
```

---

## 💡 Key Learnings

After studying this system, you'll understand:

1. ✅ How to design systems with multiple entity hierarchies
2. ✅ Why thread safety matters and how to implement it
3. ✅ When and how to apply design patterns effectively
4. ✅ How to handle real-world edge cases
5. ✅ Trade-offs between performance and scalability
6. ✅ How to explain architectural decisions

---

## 📚 Real-World Applications

This design pattern is used in:
- 🅿️ Valet parking management systems
- 🏢 Office building parking systems
- 🛒 Shopping mall parking management
- 🏨 Hotel parking systems
- ✈️ Airport parking facilities

---

## 🏁 Summary

The Parking Lot System demonstrates:
- Clean object-oriented design
- Thread-safe concurrent programming
- Practical application of design patterns
- Scalable architecture for real-world systems
- Production-quality code standards

**This is an excellent interview problem that showcases your understanding of system design at scale!**

---

**Next Steps**: Review the `demo/ParkingLotDemo.java` for practical usage examples.
