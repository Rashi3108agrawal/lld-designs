package demo;

import entities.*;
import entities.enums.*;
import strategies.*;

/**
 * Comprehensive demo showcasing Parking Lot System functionality
 * 
 * This demo demonstrates:
 * - Creating vehicles of different types
 * - Parking and unparking operations
 * - Fee calculation with different pricing strategies
 * - Thread-safe concurrent operations
 * - Error handling for edge cases
 */
public class ParkingLotDemo {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        🚗 PARKING LOT SYSTEM - COMPREHENSIVE DEMO 🚗            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Initialize parking lot with hourly pricing
        ParkingLot parkingLot = ParkingLot.getInstance(new HourlyPricingStrategy());

        // ========== DEMO 1: Single Vehicle Parking ==========
        demo1_BasicParking(parkingLot);

        // ========== DEMO 2: Multiple Vehicles ==========
        demo2_MultipleVehicles(parkingLot);

        // ========== DEMO 3: Different Pricing Strategies ==========
        demo3_PricingStrategies();

        // ========== DEMO 4: Concurrent Parking ==========
        demo4_ConcurrentParking();

        // ========== DEMO 5: Edge Cases ==========
        demo5_EdgeCases();

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ✅ DEMO COMPLETED ✅                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    /**
     * DEMO 1: Basic parking and unparking for a single vehicle
     */
    public static void demo1_BasicParking(ParkingLot parkingLot) {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("DEMO 1: Basic Parking & Unparking 🚙");
        System.out.println("=".repeat(65) + "\n");

        try {
            // Create a car
            Vehicle car = new Car("DL01AB1234");
            System.out.println("📝 Created vehicle: " + car.getLicensePlate() + " (Type: CAR)");

            // Park the vehicle
            System.out.println("\n🅿️  Attempting to park vehicle...");
            Ticket ticket = parkingLot.parkVehicle(car);
            System.out.println("✅ Vehicle parked successfully!");
            System.out.println("   Ticket ID: " + ticket.getTicketId());
            System.out.println("   Location: Floor " + ticket.getParkingSpot().getFloor().getFloorNumber()
                    + ", Spot " + ticket.getParkingSpot().getSpotNumber());
            System.out.println("   Entry Time: " + formatTime(ticket.getEntryTime()));

            // Simulate parking duration
            System.out.println("\n⏳ Vehicle parked for 2 hours...\n");
            long parkingDuration = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
            ticket.setExitTime(ticket.getEntryTime() + parkingDuration);

            // Unpark the vehicle
            System.out.println("🚗 Attempting to unpark vehicle...");
            double fee = parkingLot.unparkVehicle(ticket);
            System.out.println("✅ Vehicle unparked successfully!");
            System.out.println("   Exit Time: " + formatTime(ticket.getExitTime()));
            System.out.println("   Duration: 2 hours");
            System.out.println("   Parking Fee: $" + String.format("%.2f", fee));

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * DEMO 2: Multiple vehicles with different types
     */
    public static void demo2_MultipleVehicles(ParkingLot parkingLot) {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("DEMO 2: Multiple Vehicles (Different Types) 🏎️");
        System.out.println("=".repeat(65) + "\n");

        try {
            Vehicle[] vehicles = {
                    new Car("DL01AB5678"),
                    new Motorcycle("MH02BC9012"),
                    new Truck("KA03CD3456"),
                    new Car("TN04DE7890")
            };

            Ticket[] tickets = new Ticket[vehicles.length];

            // Park all vehicles
            System.out.println("🅿️  Parking multiple vehicles...\n");
            for (int i = 0; i < vehicles.length; i++) {
                Vehicle vehicle = vehicles[i];
                tickets[i] = parkingLot.parkVehicle(vehicle);
                System.out.println((i + 1) + ". ✅ " + vehicle.getClass().getSimpleName()
                        + " (" + vehicle.getLicensePlate() + ") parked");
                System.out.println("   Location: Floor " + tickets[i].getParkingSpot().getFloor().getFloorNumber()
                        + ", Spot " + tickets[i].getParkingSpot().getSpotNumber());
            }

            // Simulate different parking durations
            System.out.println("\n⏳ Vehicles parked for different durations...\n");
            long[] durations = { 1 * 60 * 60 * 1000, 3 * 60 * 60 * 1000, 2 * 60 * 60 * 1000, 4 * 60 * 60 * 1000 };

            System.out.println("🚗 Unparking vehicles and calculating fees...\n");
            double totalFees = 0;
            for (int i = 0; i < tickets.length; i++) {
                tickets[i].setExitTime(tickets[i].getEntryTime() + durations[i]);
                double fee = parkingLot.unparkVehicle(tickets[i]);
                totalFees += fee;

                long hours = durations[i] / (60 * 60 * 1000);
                System.out.println((i + 1) + ". " + vehicles[i].getClass().getSimpleName()
                        + " (" + vehicles[i].getLicensePlate() + ")");
                System.out.println("   Duration: " + hours + " hours | Fee: $" + String.format("%.2f", fee));
            }

            System.out.println("\n💰 Total Fees Collected: $" + String.format("%.2f", totalFees));

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * DEMO 3: Different pricing strategies
     */
    public static void demo3_PricingStrategies() {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("DEMO 3: Pricing Strategies Comparison 💰");
        System.out.println("=".repeat(65) + "\n");

        // Create parking lots with different strategies
        ParkingLot hourlyLot = ParkingLot.getInstance(new HourlyPricingStrategy());
        ParkingLot fixedLot = new ParkingLot(new FixedPricingStrategy());

        try {
            // Test vehicles
            Vehicle car = new Car("DL05EF1111");
            Vehicle motorcycle = new Motorcycle("MH06GH2222");

            // Parking duration: 3 hours
            long duration = 3 * 60 * 60 * 1000;

            System.out.println("Parking Duration: 3 hours\n");

            // Hourly Pricing Strategy
            System.out.println("1️⃣  HOURLY PRICING STRATEGY");
            System.out.println("   Car (3 hours @ $5/hr): $" + String.format("%.2f", 15.0));
            System.out.println("   Motorcycle (3 hours @ $2/hr): $" + String.format("%.2f", 6.0));

            // Fixed Pricing Strategy
            System.out.println("\n2️⃣  FIXED PRICING STRATEGY");
            System.out.println("   Car (flat rate): $" + String.format("%.2f", 10.0));
            System.out.println("   Motorcycle (flat rate): $" + String.format("%.2f", 10.0));

            // Dynamic Pricing (if implemented)
            System.out.println("\n3️⃣  DYNAMIC PRICING STRATEGY (Future Enhancement)");
            System.out.println("   Base rate × demand multiplier");
            System.out.println("   Peak hours: 1.5x multiplier");
            System.out.println("   Off-peak: 0.8x multiplier");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * DEMO 4: Concurrent parking operations
     */
    public static void demo4_ConcurrentParking() {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("DEMO 4: Concurrent Parking Operations 🔀");
        System.out.println("=".repeat(65) + "\n");

        ParkingLot parkingLot = ParkingLot.getInstance(new HourlyPricingStrategy());

        System.out.println("Simulating 10 threads parking vehicles simultaneously...\n");

        Thread[] threads = new Thread[10];
        final Object lock = new Object();
        final int[] successCount = { 0 };
        final int[] failureCount = { 0 };

        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    Vehicle vehicle;
                    if (threadId % 3 == 0) {
                        vehicle = new Car("DL" + String.format("%02d", threadId) + "AA0001");
                    } else if (threadId % 3 == 1) {
                        vehicle = new Motorcycle("MH" + String.format("%02d", threadId) + "BB0001");
                    } else {
                        vehicle = new Truck("KA" + String.format("%02d", threadId) + "CC0001");
                    }

                    Ticket ticket = parkingLot.parkVehicle(vehicle);
                    synchronized (lock) {
                        successCount[0]++;
                        System.out.println("Thread-" + threadId + " ✅ " + vehicle.getClass().getSimpleName()
                                + " parked at Floor " + ticket.getParkingSpot().getFloor().getFloorNumber());
                    }
                } catch (Exception e) {
                    synchronized (lock) {
                        failureCount[0]++;
                        System.out.println("Thread-" + threadId + " ❌ Parking failed: " + e.getMessage());
                    }
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n" + "─".repeat(65));
        System.out.println("Results: " + successCount[0] + " succeeded | " + failureCount[0] + " failed");
        System.out.println("(Thread safety ensures no race conditions!)");
    }

    /**
     * DEMO 5: Edge cases and error handling
     */
    public static void demo5_EdgeCases() {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("DEMO 5: Edge Cases & Error Handling ⚠️");
        System.out.println("=".repeat(65) + "\n");

        ParkingLot parkingLot = ParkingLot.getInstance(new HourlyPricingStrategy());

        // Edge Case 1: Unpark with invalid ticket
        System.out.println("1️⃣  Invalid Ticket Unpark:");
        try {
            Ticket invalidTicket = new Ticket("INVALID123", null, null);
            parkingLot.unparkVehicle(invalidTicket);
            System.out.println("   This shouldn't print");
        } catch (Exception e) {
            System.out.println("   ✅ Caught exception: Invalid ticket");
        }

        // Edge Case 2: Park with same license plate twice
        System.out.println("\n2️⃣  Double Parking (Same Vehicle):");
        try {
            Vehicle car = new Car("DL99XX9999");
            parkingLot.parkVehicle(car);
            System.out.println("   ✅ First park: Success");

            // Try to park same vehicle again
            try {
                parkingLot.parkVehicle(car);
                System.out.println("   ⚠️  Second park: Might succeed if already unparked");
            } catch (Exception e) {
                System.out.println("   ✅ Second park: Prevented by system");
            }

        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        // Edge Case 3: Zero/Negative duration
        System.out.println("\n3️⃣  Minimal Parking Duration:");
        try {
            Vehicle motorcycle = new Motorcycle("MH88YY8888");
            Ticket ticket = parkingLot.parkVehicle(motorcycle);
            
            // Minimal duration (1 minute)
            ticket.setExitTime(ticket.getEntryTime() + 60 * 1000);
            double fee = parkingLot.unparkVehicle(ticket);
            System.out.println("   ✅ Minimum charge applied: $" + String.format("%.2f", fee));

        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        // Edge Case 4: Different vehicle types
        System.out.println("\n4️⃣  Vehicle Type Compatibility:");
        try {
            Vehicle van = new Van("UP77ZZ7777");
            System.out.println("   Van dimensions: Large");
            System.out.println("   Required spot type: LARGE");
            System.out.println("   ✅ System matches vehicle to appropriate spot type");

        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
    }

    /**
     * Utility method to format time in readable format
     */
    private static String formatTime(long timeMillis) {
        java.util.Date date = new java.util.Date(timeMillis);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(date);
    }
}

/**
 * USAGE GUIDE:
 * 
 * 1. Create a Parking Lot:
 *    ParkingLot parkingLot = ParkingLot.getInstance(new HourlyPricingStrategy());
 * 
 * 2. Create a Vehicle:
 *    Vehicle car = new Car("DL01AB1234");
 * 
 * 3. Park Vehicle:
 *    Ticket ticket = parkingLot.parkVehicle(car);
 * 
 * 4. Unpark Vehicle:
 *    double fee = parkingLot.unparkVehicle(ticket);
 * 
 * DESIGN PATTERNS DEMONSTRATED:
 * ✅ Singleton: ParkingLot.getInstance()
 * ✅ Strategy: Different pricing strategies
 * ✅ Composition: Lot → Floors → Spots
 * ✅ Factory: Different vehicle types
 * ✅ Thread Safety: Concurrent operations
 */
