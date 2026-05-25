# 📢 Notification System – Low Level Design

A **scalable, queue-based notification system** capable of sending millions of messages across multiple channels (Email, SMS, Push Notifications) with reliability, retry mechanisms, and failure handling.

---

## 📌 Problem Statement

Design a **scalable notification system** that supports:

- ✅ Send notifications via multiple channels (Email, SMS, Push)
- ✅ Support different priorities (High, Medium, Low)
- ✅ Handle retries on failure automatically
- ✅ Rate limiting to respect external provider limits
- ✅ Track notification status and delivery
- ✅ Scale to millions of messages per day
- ✅ Decouple producers from consumers (async processing)

---

## 🎯 Core Requirements

### Functional Requirements
1. Send notifications through different channels
2. Support priority-based delivery (high priority = immediate)
3. Implement retry logic for transient failures
4. Rate limit to prevent throttling from providers
5. Track delivery status and logs
6. Support idempotency to prevent duplicate sends

### Non-Functional Requirements
1. **Scalability**: Handle millions of messages per day
2. **Reliability**: Guaranteed delivery with retry mechanism
3. **Latency**: Low latency for high-priority notifications
4. **Decoupling**: Async processing with message queues
5. **Monitoring**: Track success/failure rates
6. **Extensibility**: Easy to add new notification channels

---

## 🧱 Core Entities

### 1. **Notification**
```
Properties:
- id: UUID
- recipient: String (email/phone/device-token)
- message: String (notification content)
- channel: NotificationChannel (EMAIL, SMS, PUSH)
- priority: Priority (HIGH, MEDIUM, LOW)
- status: NotificationStatus (PENDING, SENT, FAILED, DELIVERED)
- metadata: Map (extra data like subject, attachments)
- createdAt: Timestamp
- sentAt: Timestamp (nullable)
```

### 2. **User**
```
Properties:
- id: String
- email: String
- phoneNumber: String
- deviceTokens: List<String>  (for push notifications)
- preferences: NotificationPreferences
```

### 3. **NotificationRequest**
```
Properties:
- recipient: String
- message: String
- channels: List<NotificationChannel>
- priority: Priority
- retryPolicy: RetryPolicy
```

### 4. **NotificationLog**
```
Properties:
- id: UUID
- notificationId: UUID
- status: String
- timestamp: Timestamp
- attemptCount: int
- errorMessage: String (nullable)
- nextRetryTime: Timestamp (nullable)
```

---

## 🔄 System Architecture

### **High-Level Flow**

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT / API                             │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│              API LAYER (NotificationController)                 │
│         - Validate input                                        │
│         - Build notification                                    │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│            NOTIFICATION SERVICE                                 │
│         - Apply business logic                                  │
│         - Determine priority                                    │
│         - Enrich notification metadata                          │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│          MESSAGE QUEUE (Kafka / RabbitMQ)                       │
│    - Buffer high traffic                                        │
│    - Enable async processing                                   │
│    - Partition by priority/channel                             │
└────────────────────────────┬──────────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
         ┌──────────┐  ┌──────────┐  ┌──────────┐
         │ Worker 1 │  │ Worker 2 │  │ Worker N │
         └────┬─────┘  └────┬─────┘  └────┬─────┘
              │             │             │
              └─────────────┼─────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │      CHANNEL SELECTOR (Factory)       │
        │  - EmailSender                        │
        │  - SmsSender                          │
        │  - PushNotificationSender             │
        └───────────────┬───────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ↓               ↓               ↓
    ┌────────┐     ┌────────┐     ┌──────────┐
    │  SES   │     │ Twilio │     │Firebase  │
    │(Email) │     │ (SMS)  │     │ (Push)   │
    └────────┘     └────────┘     └──────────┘
```

---

## 🧠 Design Patterns Used

### 1. **Factory Pattern** (Core)
```
Purpose: Create appropriate sender based on channel type

Usage:
- NotificationSenderFactory.getSender(NotificationChannel.EMAIL)
  → EmailSender
- NotificationSenderFactory.getSender(NotificationChannel.SMS)
  → SmsSender

Benefits:
- Decouples channel creation from business logic
- Easy to add new channels
- Centralized sender instantiation
```

### 2. **Strategy Pattern**
```
Purpose: Different delivery strategies based on priority

Implementations:
- ImmediateStrategy: Send immediately (HIGH priority)
- BatchStrategy: Batch and send periodically (LOW priority)
- ThrottledStrategy: Respect rate limits

Benefits:
- Change strategy at runtime
- Easy to add new strategies
- Separation of concerns
```

### 3. **Builder Pattern**
```
Purpose: Construct complex Notification object

Usage:
Notification notification = new NotificationBuilder()
    .recipient("user@example.com")
    .message("Order confirmed")
    .channel(NotificationChannel.EMAIL)
    .priority(Priority.HIGH)
    .retry(new RetryPolicy(maxAttempts=3, backoff=exponential))
    .build();

Benefits:
- Readable object construction
- Optional parameters
- Validation before creation
```

### 4. **Producer-Consumer Pattern** (Queue-based)
```
Purpose: Decouple producers (API) from consumers (Workers)

Components:
- Producer: Pushes notifications to queue
- Queue: Kafka/RabbitMQ buffers messages
- Consumer: Workers process messages

Benefits:
- High throughput
- Scalability (add more workers)
- Fault tolerance
- Load balancing
```

### 5. **Retry + Dead Letter Queue (DLQ) Pattern**
```
Purpose: Handle failures gracefully

Flow:
1. Send notification
2. If fails:
   - Increment retry count
   - Wait (exponential backoff)
   - Retry
3. If max retries exceeded:
   - Send to DLQ (Dead Letter Queue)
   - Alert operations team
   - Manual review required

Benefits:
- Handles transient failures
- Prevents infinite retry loops
- Tracks permanently failed messages
```

---

## ⚙️ Core Components

### 1. **Notification Controller (API Layer)**
```java
interface NotificationController {
    Response sendNotification(NotificationRequest request);
    Response sendBulkNotifications(List<NotificationRequest> requests);
    Response getNotificationStatus(String notificationId);
}
```

### 2. **Notification Service**
```java
interface NotificationService {
    String enqueueNotification(Notification notification);
    NotificationStatus getStatus(String notificationId);
    void retryFailedNotification(String notificationId);
}
```

### 3. **Channel Senders (Factory Products)**
```java
interface NotificationSender {
    boolean send(Notification notification) throws Exception;
}

// Implementations
- EmailSender extends NotificationSender
- SmsSender extends NotificationSender
- PushNotificationSender extends NotificationSender
```

### 4. **Notification Sender Factory**
```java
class NotificationSenderFactory {
    static NotificationSender getSender(NotificationChannel channel) {
        switch(channel) {
            case EMAIL: return emailSender;
            case SMS: return smsSender;
            case PUSH: return pushSender;
            default: throw new IllegalArgumentException();
        }
    }
}
```

### 5. **Worker Service**
```java
class NotificationWorker {
    void processNotification(Notification notification) {
        try {
            NotificationSender sender = factory.getSender(notification.getChannel());
            boolean success = sender.send(notification);
            
            if (success) {
                markAsDelivered(notification);
            } else {
                scheduleRetry(notification);
            }
        } catch (Exception e) {
            handleFailure(notification, e);
        }
    }
}
```

---

## 🔁 Retry Strategy

### **Exponential Backoff**
```
Attempt 1: Retry after 1 second
Attempt 2: Retry after 2 seconds
Attempt 3: Retry after 4 seconds
Attempt 4: Retry after 8 seconds
Attempt 5: Send to DLQ (max retries = 5)

Formula:
delay = initial_delay × 2^(attempt - 1)
delay = min(delay, max_delay)  // Cap at max_delay (e.g., 60s)
```

### **Benefits**
- ✅ Backs off gracefully
- ✅ Reduces load on failing service
- ✅ Respects rate limiting

---

## 🚦 Rate Limiting

### **Problem**
External providers (SES, Twilio, Firebase) have rate limits:
- AWS SES: ~14 emails/second
- Twilio: ~1000 SMS/second
- Firebase: Varies by plan

### **Solution: Token Bucket Algorithm**
```
Bucket capacity: 100 tokens
Refill rate: 10 tokens/second

Process:
1. Check if tokens available
2. If yes:
   - Consume 1 token
   - Send notification
3. If no:
   - Queue notification
   - Wait until tokens available
```

### **Implementation**
```java
class RateLimiter {
    private double tokens;
    private final double capacity;
    private final double refillRate;
    private long lastRefillTime;
    
    synchronized boolean allowRequest() {
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
                         tokens + (timePassed / 1000.0) * refillRate);
        lastRefillTime = now;
    }
}
```

---

## 🗃️ Database Schema

### **Notifications Table**
```sql
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    channel ENUM('EMAIL', 'SMS', 'PUSH') NOT NULL,
    message TEXT NOT NULL,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED', 'DELIVERED') NOT NULL,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX (recipient),
    INDEX (status),
    INDEX (created_at)
);
```

### **Notification Logs Table**
```sql
CREATE TABLE notification_logs (
    id VARCHAR(36) PRIMARY KEY,
    notification_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempt_count INT DEFAULT 0,
    error_message TEXT NULL,
    next_retry_time TIMESTAMP NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id),
    INDEX (notification_id),
    INDEX (timestamp)
);
```

---

## ⚠️ Edge Cases Handled

1. **External Provider Down** 🔴
   - Retry with exponential backoff
   - Eventually move to DLQ
   - Alert operations team

2. **Rate Limit Exceeded** 🚦
   - Queue notification
   - Wait for token bucket refill
   - Don't drop notifications

3. **Duplicate Notifications** 📍
   - Idempotency key in request
   - Check database for duplicates
   - Return existing notification ID

4. **Worker Crash During Processing** 💥
   - Message stays in queue
   - Redelivered to another worker
   - No loss of data

5. **Queue Backlog Overload** 📊
   - Horizontal scale workers
   - Partition queue by priority
   - High-priority notifications processed first

6. **Retry Storm** 🌊
   - Max retry limit (e.g., 5 attempts)
   - Exponential backoff prevents hammering
   - DLQ for manual review

7. **Invalid Recipient** ❌
   - Validate email/phone format
   - Skip invalid recipients
   - Log for audit trail

---

## 📊 Monitoring & Metrics

### **Key Metrics to Track**
```
1. Delivery Rate
   - (Delivered / Total Sent) × 100
   
2. Failure Rate
   - (Failed / Total Sent) × 100
   
3. Latency
   - Time from enqueue to sent
   - Time from sent to delivered
   
4. Queue Depth
   - Number of pending notifications
   - Alert if > threshold
   
5. Worker Health
   - CPU/Memory usage
   - Message processing rate
```

---

## 🚀 Scalability Considerations

### **Horizontal Scaling**
```
1. Add more workers
   - Process queue in parallel
   - Independent scaling per channel

2. Partition by channel
   - Email queue
   - SMS queue
   - Push queue
   - Dedicated workers per queue

3. Partition by priority
   - High-priority queue (SLA: < 1 minute)
   - Medium-priority queue (SLA: < 5 minutes)
   - Low-priority queue (SLA: < 1 hour)
```

### **Vertical Scaling**
```
1. Batch processing
   - Group notifications
   - Send multiple at once
   - Reduce API calls

2. Connection pooling
   - Reuse connections to providers
   - Reduce overhead

3. Caching
   - Cache user preferences
   - Cache channel availability
```

---

## 🧾 Interview Talking Points

### **Opening Statement** 🎤
> "I designed a scalable notification system using a queue-based architecture to handle high throughput. Notifications are built using the Builder Pattern and routed through a Factory Pattern-based sender selection. The system uses asynchronous processing with Kafka/RabbitMQ to decouple producers from consumers. For reliability, I implemented an exponential backoff retry mechanism with a Dead Letter Queue for permanently failed messages. Rate limiting is handled using the Token Bucket algorithm to respect external provider limits."

### **Key Points to Emphasize**
✅ Why queue-based architecture (decoupling + scalability)  
✅ How retries and DLQ prevent message loss  
✅ Rate limiting strategy and implementation  
✅ Idempotency for preventing duplicates  
✅ Horizontal scaling through worker replication  
✅ Monitoring and alerting  

### **Follow-up Questions to Expect**
1. **"How to ensure exactly-once delivery?"**
   - Idempotency keys
   - Database deduplication
   - Distributed consensus (complex)

2. **"What if a worker dies mid-processing?"**
   - Message returned to queue
   - Picked up by another worker
   - Consumer group rebalancing

3. **"How to prioritize high-priority messages?"**
   - Separate queues by priority
   - Dedicated workers per priority
   - Worker picks from high-priority queue first

4. **"How to handle provider-specific rate limits?"**
   - Per-provider rate limiters
   - Backpressure mechanism
   - Dynamic rate adjustment

---

## 📂 Project Structure

```
NotificationSystem/
├── src/
│   ├── entities/
│   │   ├── Notification.java
│   │   ├── User.java
│   │   ├── NotificationRequest.java
│   │   ├── NotificationLog.java
│   │   └── enums/
│   │       ├── NotificationChannel.java
│   │       ├── Priority.java
│   │       └── NotificationStatus.java
│   ├── services/
│   │   ├── NotificationService.java
│   │   ├── NotificationController.java
│   │   └── NotificationWorker.java
│   ├── senders/
│   │   ├── NotificationSender.java (interface)
│   │   ├── EmailSender.java
│   │   ├── SmsSender.java
│   │   ├── PushNotificationSender.java
│   │   └── NotificationSenderFactory.java
│   ├── strategies/
│   │   ├── Strategy.java
│   │   ├── ImmediateStrategy.java
│   │   ├── BatchStrategy.java
│   │   └── ThrottledStrategy.java
│   ├── ratelimiter/
│   │   └── RateLimiter.java
│   ├── retry/
│   │   ├── RetryPolicy.java
│   │   └── ExponentialBackoffRetry.java
│   ├── queue/
│   │   ├── MessageQueue.java (interface)
│   │   ├── KafkaQueue.java
│   │   └── RabbitMQQueue.java
│   ├── demo/
│   │   └── NotificationSystemDemo.java
│   └── test/
│       └── NotificationSystemTest.java
├── README.md (this file)
├── notes.md
└── [Additional code will be added]
```

---

## 🔧 Getting Started

### Prerequisites
- Java 8+
- Kafka or RabbitMQ
- Understanding of:
  - Message queues
  - Async processing
  - Design patterns

### Running Demo
```bash
cd NotificationSystem
javac -d bin src/**/*.java
java -cp bin demo.NotificationSystemDemo
```

---

## 💡 Real-World Applications

This system powers:
- 📧 **Email notifications** in e-commerce (order updates)
- 📱 **SMS alerts** in banking (transaction confirmations)
- 🔔 **Push notifications** in social media (friend requests)
- ⏰ **Reminders** in appointment apps (upcoming events)
- 🎉 **Marketing campaigns** (promotional messages)

---

## 🏁 Summary

The Notification System demonstrates:
- High-throughput async architecture
- Reliable message delivery with retries
- Clean separation through design patterns
- Real-world distributed system challenges
- Production-quality code standards

**Time to Implement**: ~4-5 hours for full solution  
**Difficulty**: ⭐⭐⭐⭐☆ (Hard)  
**Interview Frequency**: ⭐⭐⭐⭐☆ (Common in backend interviews)

**Next Steps**: Review `demo/NotificationSystemDemo.java` for practical usage examples.
