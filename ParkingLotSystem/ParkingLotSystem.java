import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// ---------------- ENUMS ----------------
enum VehicleType {
    CAR, TRUCK, MOTORCYCLE, VAN
}

enum ParkingSpotType {
    COMPACT, LARGE, HANDICAPPED, MOTORCYCLE
}

// ---------------- VEHICLE ----------------
abstract class Vehicle {
    private final String licensePlate;
    private final VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public VehicleType getType() {
        return type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
}

class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }
}

class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }
}

class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }
}

// ---------------- TICKET ----------------
class Ticket {
    private final String id;
    private final Vehicle vehicle;
    private final long entryTime;
    private final ParkingSpot spot;

    public Ticket(String id, Vehicle vehicle, ParkingSpot spot) {
        this.id = id;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }

    public long getEntryTime() {
        return entryTime;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public String getId() {
        return id;
    }
}

// ---------------- PARKING SPOT ----------------
class ParkingSpot {
    private final int id;
    private final ParkingSpotType type;
    private Vehicle vehicle;
    private final Lock lock = new ReentrantLock();

    public ParkingSpot(int id, ParkingSpotType type) {
        this.id = id;
        this.type = type;
    }

    public boolean canFit(Vehicle vehicle) {
        if (this.vehicle != null) return false;

        switch (vehicle.getType()) {
            case MOTORCYCLE:
                return true;
            case CAR:
                return type == ParkingSpotType.COMPACT || type == ParkingSpotType.LARGE;
            case TRUCK:
                return type == ParkingSpotType.LARGE;
            default:
                return false;
        }
    }

    public boolean park(Vehicle vehicle) {
        lock.lock();
        try {
            if (this.vehicle == null && canFit(vehicle)) {
                this.vehicle = vehicle;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void unpark() {
        lock.lock();
        try {
            this.vehicle = null;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFree() {
        return vehicle == null;
    }
}

// ---------------- FLOOR ----------------
class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots = new ArrayList<>();

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    public ParkingSpot findSpot(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (spot.isFree() && spot.canFit(vehicle)) {
                return spot;
            }
        }
        return null;
    }
}

// ---------------- PRICING STRATEGY ----------------
interface PricingStrategy {
    double calculatePrice(Ticket ticket);
}

class HourlyPricingStrategy implements PricingStrategy {
    private final double motorcycleRate = 10;
    private final double carRate = 20;
    private final double truckRate = 30;

    @Override
    public double calculatePrice(Ticket ticket) {
        long durationHours =
                Math.max(1,
                        (System.currentTimeMillis() - ticket.getEntryTime()) / (1000 * 60 * 60)
                );

        switch (ticket.getVehicle().getType()) {
            case MOTORCYCLE:
                return durationHours * motorcycleRate;
            case CAR:
                return durationHours * carRate;
            case TRUCK:
                return durationHours * truckRate;
            default:
                return durationHours * carRate;
        }
    }
}

// ---------------- PARKING LOT (THREAD SAFE SINGLETON) ----------------
class ParkingLot {
    private static volatile ParkingLot instance;

    private final List<ParkingFloor> floors = new ArrayList<>();
    private final ConcurrentHashMap<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ParkingSpot> vehicleSpotMap = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    private final PricingStrategy pricingStrategy = new HourlyPricingStrategy();

    private ParkingLot() {}

    // Thread-safe Singleton (Double Checked Locking)
    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) {
                    instance = new ParkingLot();
                }
            }
        }
        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    // ---------------- PARK VEHICLE ----------------
    public Ticket parkVehicle(Vehicle vehicle) {
        lock.lock();
        try {
            for (ParkingFloor floor : floors) {
                ParkingSpot spot = floor.findSpot(vehicle);

                if (spot != null && spot.park(vehicle)) {

                    Ticket ticket = new Ticket(
                            UUID.randomUUID().toString(),
                            vehicle,
                            spot
                    );

                    activeTickets.put(ticket.getId(), ticket);
                    vehicleSpotMap.put(vehicle.getLicensePlate(), spot);

                    return ticket;
                }
            }
            return null; // no spot available
        } finally {
            lock.unlock();
        }
    }

    // ---------------- UNPARK VEHICLE ----------------
    public double unparkVehicle(String licensePlate) {
        lock.lock();
        try {
            ParkingSpot spot = vehicleSpotMap.get(licensePlate);
            if (spot == null) return 0;

            Ticket ticketToRemove = null;

            for (Ticket t : activeTickets.values()) {
                if (t.getVehicle().getLicensePlate().equals(licensePlate)) {
                    ticketToRemove = t;
                    break;
                }
            }

            if (ticketToRemove == null) return 0;

            double price = pricingStrategy.calculatePrice(ticketToRemove);

            spot.unpark();

            activeTickets.remove(ticketToRemove.getId());
            vehicleSpotMap.remove(licensePlate);

            return price;
        } finally {
            lock.unlock();
        }
    }
}