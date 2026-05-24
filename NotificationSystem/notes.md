# 📢 Notification System – Low Level Design (LLD)

---

## 📌 Overview

Design a **scalable notification system** capable of sending millions of messages across multiple channels:

- 📩 Email
- 📱 SMS
- 🔔 Push Notifications

### 🎯 Core Requirements

- Send notifications via multiple channels
- Support different priorities (High, Medium, Low)
- Handle retries on failure
- Support rate limiting (external providers like Twilio, AWS SES)
- Track notification status
- Ensure scalability for millions of messages

---

## 🧱 Key System Goals

- High throughput message processing
- Reliable delivery with retry mechanism
- Decoupled architecture using queues
- Extensible design for new channels

---

## 🧩 Core Entities

### 1. Notification
- id
- recipient
- message
- channel type
- priority
- status (PENDING, SENT, FAILED)

---

### 2. User
- id
- contact info (email, phone, device token)

---

### 3. NotificationRequest
- wraps input payload
- used to build Notification object

---

### 4. NotificationLog
- tracks delivery status
- retry count
- timestamps

---

## 🔄 System Flow

### Step 1: Create Notification
- Build notification using Builder Pattern

### Step 2: Queueing
- Push notification into message queue (Kafka / RabbitMQ)

### Step 3: Processing
- Worker consumes messages
- Selects appropriate sender (Factory Pattern)

### Step 4: Sending
- Channel-specific sender executes delivery

### Step 5: Retry / Failure Handling
- Retry if failure occurs
- If max retries exceeded → send to DLQ (Dead Letter Queue)

---

## 🧠 Design Patterns Used

---

### 🔥 1. Factory Pattern (CORE)

Used to create channel-specific senders:

- EmailSender
- SmsSender
- PushNotificationSender

### Why?
- Avoids tight coupling
- Easy to add new channels

---

### 🔥 2. Strategy Pattern

Used for:
- Priority-based delivery
- Channel-specific delivery logic

Example:
- High priority → immediate send
- Low priority → batch processing

---

### 🔥 3. Builder Pattern

Used to construct complex Notification object:

- recipient
- message
- channel
- priority
- metadata

---

### 🔥 4. Queue / Worker Pattern (VERY IMPORTANT)

Used for scalability:

- Producers → push messages
- Consumers → process asynchronously

Ensures:
- High throughput
- Decoupling of services

---

### 🔥 5. Retry + DLQ Pattern

Used for reliability:

- Retry on transient failures
- Exponential backoff
- Dead Letter Queue for permanent failures

---

## 🧱 High-Level Architecture
Client → API Layer → Notification Service → Queue → Worker → Channel Sender → External Provider
---

## ⚙️ Components

### 1. API Layer
- Accepts notification request
- Validates input
- Sends to queue

---

### 2. Notification Service
- Builds notification object
- Assigns priority
- Pushes to queue

---

### 3. Message Queue
- Kafka / RabbitMQ
- Buffers high traffic
- Enables async processing

---

### 4. Worker Service
- Consumes messages
- Applies retry logic
- Calls appropriate sender

---

### 5. Channel Senders
- Email Sender
- SMS Sender
- Push Sender

---

## 🗃️ Database Design

### Notification Table

| Column | Type |
|--------|------|
| id | UUID |
| recipient | String |
| channel | ENUM |
| message | TEXT |
| priority | ENUM |
| status | ENUM |
| retry_count | INT |
| created_at | TIMESTAMP |

---

### Notification Status Table (Optional)

- SENT
- FAILED
- RETRYING
- DELIVERED

---

## ⚠️ Edge Cases

- External API failure (Twilio / SES down)
- Rate limit exceeded
- Duplicate notifications
- Worker crash during processing
- Queue backlog overload
- Retry storm (too many retries)

---

## 🚦 Rate Limiting Strategy

- Token bucket or leaky bucket algorithm
- Per provider limits
- Per user limits

---

## 🔁 Retry Strategy

- Exponential backoff
- Max retry count (e.g., 3–5)
- Move to DLQ after failure threshold

---

## 📊 Scalability Considerations

- Horizontal scaling of workers
- Partitioned queues (by region or priority)
- Batch processing for low priority notifications
- CDN or third-party provider fallback

---

## 💡 Real-World Usage

- OTP systems
- Email verification flows
- Order updates (e-commerce)
- Social media alerts
- Banking alerts

---

## 🧠 Interview Tips

### Must Explain:

✔ Why queue is needed (decoupling + scaling)  
✔ Why async processing is required  
✔ How retries + DLQ work  
✔ How rate limiting is handled  

---

### 🔥 Bonus Points to Mention:

- Idempotency (avoid duplicate sends)
- Monitoring & alerting system
- Metrics: delivery rate, failure rate
- Multi-region failover

---

## 🧾 Interview Explanation Script

> “I designed a scalable notification system using a queue-based architecture to handle high throughput. Notifications are built using the Builder Pattern and routed through a Factory-based channel selection. A worker consumes messages asynchronously and applies retry logic with exponential backoff. Failed messages after max retries are pushed to a Dead Letter Queue. The system supports rate limiting to handle external provider constraints and is horizontally scalable using multiple workers.”

🔥 Design Patterns Used
Factory Pattern → Sender creation
Strategy Pattern → Channel behavior
Builder Pattern → Notification creation
Producer-Consumer Pattern → Queue + Worker
⚙️ System Design Concepts
Async processing
Queue-based architecture
Retry mechanism
Dead Letter Queue (DLQ)
Priority-based processing
🚀 Complexity / Scalability
O(1) enqueue
Horizontal scaling possible (add more workers)
Supports millions of messages via queue

---

## 🏁 Summary

This system demonstrates:

- High scalability architecture
- Reliable message delivery
- Clean separation of concerns
- Real-world distributed system design

It is widely used in:
- Messaging platforms
- E-commerce alerts
- Banking systems
- Social media notifications

---