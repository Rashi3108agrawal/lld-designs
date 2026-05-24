package NotificationSystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// ---------------- ENUMS ----------------
enum ChannelType {
    EMAIL, SMS, PUSH
}

enum Priority {
    HIGH, MEDIUM, LOW
}

enum Status {
    PENDING, SENT, FAILED, RETRYING, DEAD_LETTER
}

// -------------------------NOTIFICATION BUILDER-------------------
class Notification {
    String id;
    String recipient;
    String message;
    ChannelType channel;
    Priority priority;
    Status status;
    AtomicInteger retryCount = new AtomicInteger(0);

    private Notification(NotificationBuilder builder) {
        this.id = UUID.randomUUID().toString();
        this.recipient = builder.recipient;
        this.message = builder.message;
        this.channel = builder.channel;
        this.priority = builder.priority;
        this.status = Status.PENDING;
    }

    public static class NotificationBuilder {
        String recipient;
        String message;
        ChannelType channel;
        Priority priority;

        public NotificationBuilder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationBuilder channel(ChannelType channel) {
            this.channel = channel;
            return this;
        }

        public NotificationBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}


// --------------SENDER INTERFACE (STRATEGY PATTERN)--------------
interface NotificationSender {
    boolean send(Notification notification);
}

// --------------------------CONCRETE SENDER IMPLEMENTATIONS-------------------
class EmailSender implements NotificationSender {
    public boolean send(Notification notification) {
        System.out.println("Sending EMAIL to " + notification.recipient);
        return true;
    }
}

class SmsSender implements NotificationSender {
    public boolean send(Notification notification) {
        System.out.println("Sending SMS to " + notification.recipient);
        return true;
    }
}

class PushSender implements NotificationSender {
    public boolean send(Notification notification) {
        System.out.println("Sending PUSH to " + notification.recipient);
        return true;
    }
}

//------------------NOTIFICATION SENDER FACTORY-------------------
class NotificationSenderFactory {
    public static NotificationSender getSender(ChannelType type) {
        switch (type) {
            case EMAIL:
                return new EmailSender();
            case SMS:
                return new SmsSender();
            case PUSH:
                return new PushSender();
            default:
                throw new IllegalArgumentException("Invalid channel");
        }
    }
}

//-----------------NOTIFICATION QUEUE /WORKER-------------------
class NotificationService {

    private final BlockingQueue<Notification> queue =
            new PriorityBlockingQueue<>(100,
                    Comparator.comparing(n -> n.priority.ordinal()));

    private final BlockingQueue<Notification> deadLetterQueue =
            new LinkedBlockingQueue<>();

    private final int MAX_RETRY = 3;

    public NotificationService() {
        startWorkers();
    }

    /* ===================== PRODUCER ===================== */
    public void send(Notification notification) {
        queue.offer(notification);
    }

    /* ===================== WORKERS ===================== */
    private void startWorkers() {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 3; i++) {
            executor.submit(this::process);
        }
    }

    private void process() {
        while (true) {
            try {
                Notification notification = queue.take();
                handle(notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* ===================== PROCESSING ===================== */
    private void handle(Notification notification) {

        NotificationSender sender =
                NotificationSenderFactory.getSender(notification.channel);

        boolean success = sender.send(notification);

        if (success) {
            notification.status = Status.SENT;
        } else {
            retry(notification);
        }
    }

    /* ===================== RETRY + DLQ ===================== */
    private void retry(Notification notification) {

        int count = notification.retryCount.incrementAndGet();

        if (count <= MAX_RETRY) {
            notification.status = Status.RETRYING;
            queue.offer(notification); // requeue
        } else {
            notification.status = Status.DEAD_LETTER;
            deadLetterQueue.offer(notification);
            System.out.println("DLQ → " + notification.id);
        }
    }
}