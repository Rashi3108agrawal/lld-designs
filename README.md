# 🏗️ Low Level Design (LLD) - System Design Implementations

A comprehensive repository for practicing **Low Level System Design and Object-Oriented Design principles** through carefully crafted real-world system implementations.

This repository contains practical implementations of common LLD interview problems with a focus on clean architecture, SOLID principles, and scalable design patterns.

---

## 📌 Purpose

This repository serves multiple goals:

- 🎓 **Master OOP Concepts**: Deepen understanding of Object-Oriented Programming fundamentals
- 🛠️ **Learn Design Patterns**: Implement and practice Gang of Four design patterns in real systems
- ✅ **Apply SOLID Principles**: Build code that is maintainable, testable, and extensible
- 💼 **Ace System Design Interviews**: Prepare for LLD rounds with well-documented, production-quality code
- 🔄 **Improve Code Quality**: Develop skills in clean code, proper abstraction, and architectural thinking

---

## 🛡️ Tech Stack

- **Language**: Java
- **Design Patterns**: Strategy, Factory, Singleton, Observer, State, Command, etc.
- **Principles**: SOLID, DRY, KISS, YAGNI
- **Architecture**: Layered design with clear separation of concerns

---

## 🧠 Design Philosophy

> "Good design is not about solving only current requirements, but about building systems that can evolve easily over time."

Every design in this repository follows this systematic approach:

1. **Understand Requirements** - Clarify functional and non-functional requirements
2. **Identify Core Entities** - Extract key domain objects and their attributes
3. **Define Relationships** - Map interactions and dependencies between entities
4. **Design Classes** - Assign responsibilities using Single Responsibility Principle
5. **Apply OOP Principles** - Leverage Encapsulation, Inheritance, Polymorphism, Abstraction
6. **Use Design Patterns** - Introduce patterns to solve recurring design problems
7. **Consider Edge Cases** - Account for boundary conditions and exceptional scenarios
8. **Ensure Extensibility** - Design systems that can accommodate future requirements

---

## 📂 Repository Structure

```
lld-designs/
├── parking-lot/              # Parking lot management system
│   ├── src/
│   │   ├── entities/        # Core domain entities
│   │   ├── services/        # Business logic services
│   │   ├── strategies/      # Strategy pattern implementations
│   │   ├── exceptions/      # Custom exceptions
│   │   └── Main.java        # Demo/Test driver
│   └── README.md            # Design details
├── README.md                # This file
└── .gitignore
```

---

## 🎯 Systems Designed

### 1. **Parking Lot Management System** 📍
A complete parking lot system with support for multiple entry/exit gates, different vehicle types, and flexible pricing strategies.

**Key Features:**
- Multiple parking levels and slots
- Support for different vehicle types (Car, Bike, Truck)
- Entry and exit management
- Dynamic pricing strategies (Hourly, Fixed, Per-Level)
- Reservation system
- Parking spot search algorithms

**Design Patterns Used:**
- Factory Pattern (for creating vehicles and strategies)
- Strategy Pattern (for pricing algorithms)
- Singleton Pattern (for parking lot instance)

[👉 View Parking Lot Design](./parking-lot)

---

## 📚 Core Design Concepts

### Object-Oriented Programming (OOP)
- **Encapsulation**: Hide internal state, expose necessary interfaces
- **Inheritance**: Create hierarchies for code reuse (Vehicle hierarchy)
- **Polymorphism**: Use method overriding for different vehicle behaviors
- **Abstraction**: Abstract away complex implementations behind simple interfaces

### SOLID Principles

| Principle | Implementation |
|-----------|-----------------|
| **S** - Single Responsibility | Each class has one reason to change |
| **O** - Open/Closed | Open for extension, closed for modification |
| **L** - Liskov Substitution | Subtypes can replace supertypes seamlessly |
| **I** - Interface Segregation | Client-specific interfaces instead of fat interfaces |
| **D** - Dependency Inversion | Depend on abstractions, not concrete classes |

### Design Patterns Used

```
Strategy Pattern   → Dynamic algorithm selection at runtime
Factory Pattern    → Object creation abstraction
Singleton Pattern  → Single instance guarantees
Observer Pattern   → Event-driven architecture
State Pattern      → Behavior based on internal state
Command Pattern    → Encapsulate requests as objects
```

---

## 🏗️ For Each System Design You'll Find

- **Requirements Document** - Functional & non-functional requirements
- **Entity Identification** - Core domain entities with relationships
- **Class Design** - UML-style architecture explanation
- **Complete Implementation** - Production-quality Java code
- **Design Decisions** - Rationale behind architectural choices
- **Edge Case Handling** - How the system handles boundary conditions
- **Extensibility Notes** - How to add new features without breaking existing code

---

## 📖 How to Use This Repository

1. **Study the System**: Read the design document for the system you're interested in
2. **Understand the Architecture**: Review the class structure and relationships
3. **Read the Code**: Go through the implementation with focus on design patterns
4. **Run Examples**: Execute the main demo to see the system in action
5. **Try Modifications**: Add features or improve the design yourself
6. **Compare Solutions**: Learn from different approaches to the same problem

---

## 💡 Learning Outcomes

After exploring this repository, you'll understand:

- ✅ How to identify entities and relationships from requirements
- ✅ How to structure classes using OOP principles
- ✅ When and how to apply design patterns effectively
- ✅ How to handle edge cases and failures gracefully
- ✅ How to design systems that are easy to extend and maintain
- ✅ How to write production-quality Java code with clean architecture

---

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher
- Basic understanding of OOP concepts
- Familiarity with design patterns (helpful but not required)

### Clone & Run
```bash
git clone https://github.com/Rashi3108agrawal/lld-designs.git
cd lld-designs
cd parking-lot
# Compile and run the implementation
javac -d bin src/**/*.java
java -cp bin Main
```

---

## 🤝 Best Practices Demonstrated

- **Clean Code**: Meaningful names, small methods, DRY principle
- **Error Handling**: Custom exceptions and graceful failure handling
- **Documentation**: Clear comments explaining design decisions
- **Testability**: Loosely coupled components that are easy to unit test
- **Maintainability**: Easy to understand, modify, and extend

---

## 📝 Key Takeaways

1. **Design Before Coding** - Always clarify requirements and plan architecture
2. **Use Appropriate Patterns** - Patterns solve real problems, don't force them
3. **Think About Extensibility** - How will this system grow in the future?
4. **Keep It Simple** - KISS principle prevents over-engineering
5. **Document Your Decisions** - Future readers (including yourself) will thank you

---

## 📚 Resources & References

- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Design Patterns: Elements of Reusable Object-Oriented Software](https://en.wikipedia.org/wiki/Design_Patterns)
- [Clean Code by Robert C. Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [Refactoring: Improving the Design of Existing Code](https://refactoring.com/)

---

## 🎓 Interview Preparation Tips

- ✅ Practice explaining your design decisions clearly
- ✅ Be ready to handle follow-up questions and modifications
- ✅ Know the trade-offs between different design approaches
- ✅ Think about scalability and performance implications
- ✅ Write clean, readable code that matches your verbal explanation

---

## 📧 Questions & Feedback

Feel free to open issues for:
- Questions about any design
- Suggestions for improvement
- Bug reports in implementations
- Ideas for new LLD problems

---

## 📄 License

This repository is open source and available under the MIT License. Feel free to use it for learning purposes!

---

**Happy Learning! 🎯**

Remember: *The goal isn't just to pass interviews, but to build a solid foundation in system design that makes you a better engineer.*
