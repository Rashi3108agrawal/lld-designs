# 🚗 Parking Lot System (Thread-Safe LLD) – Interview Notes

## 📌 1. Problem Statement Summary

Design a **Parking Lot System** that supports:

- Multiple floors
- Multiple parking spot types (Compact, Large, Handicapped, Motorcycle)
- Multiple vehicle types (Car, Truck, Motorcycle, Van)
- Parking and unparking vehicles
- Ticket generation on entry
- Fee calculation based on parking duration
- Thread-safe operations for concurrent access

---

## 🧱 2. Core Entities

### Main Classes
- `ParkingLot` (Singleton, system controller)
- `ParkingFloor`
- `ParkingSpot`
- `Vehicle` (Abstract)
  - Car, Truck, Motorcycle
- `Ticket`
- `PricingStrategy`

---

## 🔗 3. Relationships (Very Important)

- ParkingLot **HAS-A** multiple ParkingFloors (Composition)
- ParkingFloor **HAS-A** multiple ParkingSpots (Composition)
- ParkingSpot **ASSIGNS** one Vehicle at a time
- Ticket **ASSOCIATES** Vehicle + ParkingSpot + Entry Time
- PricingStrategy **USED BY** ParkingLot for fee calculation

---

## ⚙️ 4. Key Functionalities

### 🚗 Parking Flow
1. Find suitable floor
2. Find suitable available spot
3. Lock spot (thread-safe)
4. Assign vehicle
5. Generate ticket
6. Store mappings

### 🚙 Unparking Flow
1. Fetch ticket using license plate
2. Calculate fee using strategy
3. Free parking spot
4. Remove mappings
5. Return final price

---

## 🧵 5. Concurrency / Thread Safety

### Why needed?
Multiple vehicles may try to park/unpark simultaneously.

### Solutions used:

#### ✔ ReentrantLock
- Ensures atomic parking/unparking operations
- Prevents race conditions on spot assignment

#### ✔ ConcurrentHashMap
- Stores active tickets safely
- Stores vehicle-to-spot mapping safely

#### ✔ Singleton (Double Checked Locking)
- Ensures only one ParkingLot instance globally

---

## 🎯 6. Design Patterns Used

### 1. Singleton Pattern
- Ensures single ParkingLot instance

### 2. Strategy Pattern
- Used for fee calculation
- Example: `HourlyPricingStrategy`

### 3. Composition
- ParkingLot → Floors → Spots

### 4. Polymorphism
- Vehicle type handling
- Pricing based on vehicle type

---

## 🧠 7. OOP Principles Applied

### ✔ Encapsulation
- Private state in ParkingSpot, Ticket

### ✔ Abstraction
- Vehicle as abstract class
- PricingStrategy interface

### ✔ Inheritance
- Car, Truck, Motorcycle extend Vehicle

### ✔ Polymorphism
- Runtime pricing behavior

---

## ⚠️ 8. Edge Cases Handled

- No available parking spot
- Concurrent parking requests for same spot
- Invalid/unmapped vehicle during unpark
- Double unpark attempt
- Spot already occupied
- Ticket not found or expired scenario (extensible)

---

## ⏱️ 9. Time Complexity

### Parking
- Worst case: **O(F × S)**
  - F = floors
  - S = spots per floor

### Unparking
- HashMap lookup: **O(1)** average

---

## 💰 10. Pricing Strategy

- Based on duration:
  - `System.currentTimeMillis()`
- Vehicle-based rate system:
  - Motorcycle → low
  - Car → medium
  - Truck → high

### Extensible:
- Hourly pricing
- Daily pricing
- Weekend pricing
- Dynamic surge pricing (future enhancement)

---

## 🚀 11. Scalability Improvements (Discussion Points)

You can mention in interview:

### 🔥 Possible upgrades:
- Add Reservation system
- Add Payment gateway integration
- Support multiple parking lots (city-wide system)
- Add EV charging spots
- Add display boards for availability
- Microservice-based architecture

---

## 🧾 12. Key Interview Summary (Say This)

> “I designed a thread-safe Parking Lot system using Singleton for global access, Composition for hierarchy, and Strategy Pattern for flexible pricing. Concurrency is handled using ReentrantLock and ConcurrentHashMap to ensure safe parking and unparking operations under multi-threaded conditions. The design is scalable and follows SOLID principles, making it extensible for future features like dynamic pricing and multi-lot systems.”

---

## ⭐ 13. What Makes This Design Strong

- Clean OOP structure
- Real-world modeling
- Thread-safe operations
- Extensible pricing system
- Production-like concurrency handling
- Clear separation of concerns

---