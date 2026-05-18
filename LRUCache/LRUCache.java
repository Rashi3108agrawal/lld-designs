package LRUCache;

import java.util.*;

public class LRUCache {

    // ---------------- NODE ----------------
    class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    // ---------------- DATA STRUCTURES ----------------
    private final int capacity;
    private final Map<Integer, Node> cache;

    private final Node head;
    private final Node tail;

    // Lock for thread safety
    private final Object lock = new Object();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();

        head = new Node(-1, -1);
        tail = new Node(-1, -1);

        head.next = tail;
        tail.prev = head;
    }

    // ---------------- PUBLIC API ----------------

    public int get(int key) {
        synchronized (lock) {
            if (!cache.containsKey(key)) return -1;

            Node node = cache.get(key);
            moveToFront(node);
            return node.value;
        }
    }

    public void put(int key, int value) {
        synchronized (lock) {
            if (capacity == 0) return;

            if (cache.containsKey(key)) {
                Node node = cache.get(key);
                node.value = value;
                moveToFront(node);
                return;
            }

            if (cache.size() == capacity) {
                Node lru = tail.prev;
                removeNode(lru);
                cache.remove(lru.key);
            }

            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addToFront(newNode);
        }
    }

    // ---------------- INTERNAL HELPERS ----------------

    private void moveToFront(Node node) {
        removeNode(node);
        addToFront(node);
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}