import java.util.Scanner;
import java.util.Vector;

class Room {
    private int roomNumber;
    private String type;
    private boolean available;
    private double price;

    public Room(int roomNumber, String type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.available = true;
        switch (type) {
            case "Single":
                this.price = 1000;
                break;
            case "Double":
                this.price = 1700;
                break;
            case "Suite":
                this.price = 3000;
                break;
            default:
                this.price = 0;
        }
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getType() {
        return type;
    }

    public boolean isAvailable() {
        return available;
    }

    public double getPrice() {
        return price;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ") - " + (available ? "Available" : "Booked");
    }
}

class Customer {
    private String name;
    private String contact;
    private String address;
    private String email;

    public Customer(String name, String contact, String address, String email) {
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + contact + ", " + address + ", " + email + ")";
    }
}

class Booking {
    private Vector<Room> rooms;
    private Customer customer;

    public Booking(Vector<Room> rooms, Customer customer) {
        this.rooms = rooms;
        this.customer = customer;
    }

    public Vector<Room> getRooms() {
        return rooms;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        StringBuilder details = new StringBuilder("Customer: " + customer + "\nRooms: ");
        for (Room room : rooms) {
            details.append(room.getRoomNumber()).append(" ");
        }
        return details.toString();
    }
}

class SerenitySuitesHotelManagementSystem {
    private Vector<Room> rooms = new Vector<>();
    private Vector<Booking> bookings = new Vector<>();

    public SerenitySuitesHotelManagementSystem() {
        for (int i = 0; i < 15; i++) {
            int roomNumber = 101 + i;
            String type = (roomNumber % 3 == 0) ? "Suite" : (roomNumber % 2 == 0) ? "Double" : "Single";
            rooms.add(new Room(roomNumber, type));
        }
    }

    public void viewAvailableRooms() {
        System.out.println("\nAvailable Rooms:");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.println(room);
            }
        }
    }

    public void cancelBooking() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter customer name to cancel booking: ");
        String name = scanner.nextLine();

        Booking bookingToCancel = null;
        for (Booking booking : bookings) {
            if (booking.getCustomer().getName().equalsIgnoreCase(name)) {
                bookingToCancel = booking;
                break;
            }
        }

        if (bookingToCancel != null) {
            for (Room room : bookingToCancel.getRooms()) {
                room.setAvailable(true);
            }
            bookings.remove(bookingToCancel);
            System.out.println("Booking canceled successfully.");
        } else {
            System.out.println("No booking found for the customer name: " + name);
        }
    }

    public void searchBookingByCustomer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter customer name to search: ");
        String name = scanner.nextLine();

        for (Booking booking : bookings) {
            if (booking.getCustomer().getName().equalsIgnoreCase(name)) {
                System.out.println("Booking found:\n" + booking);
                return;
            }
        }
        System.out.println("No booking found for the customer name: " + name);
    }

    public void listAllRooms() {
        System.out.println("\nAll Rooms:");
        for (Room room : rooms) {
            System.out.println(room);
        }
    }

    public void addRoom() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter new room number: ");
        int roomNumber = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter room type (Single/Double/Suite): ");
        String type = scanner.nextLine();

        Room newRoom = new Room(roomNumber, type);
        rooms.add(newRoom);
        System.out.println("Room added successfully: " + newRoom);
    }

    public void bookRoom() {
        Scanner scanner = new Scanner(System.in);

        
        String name;
        while (true) {
            System.out.print("Enter customer name: ");
            name = scanner.nextLine().trim();

            // Regex: Ensure the name is at least two words, each word contains only alphabetic characters and starts with a capital letter
            if (name.matches("^[A-Z][a-z]+( [A-Z][a-z]+)+$")) {
                break;
            } else {
                System.out.println("Invalid name. Please enter a valid name (e.g., John Doe). Each word should start with a capital letter and contain alphabetic characters only.");
            }
        }

        
        String contact;
        while (true) {
            System.out.print("Enter contact (10-digit number): ");
            contact = scanner.nextLine().trim();
            if (contact.matches("\\d{10}")) break;
            System.out.println("Invalid contact number. Please enter exactly 10 digits (no letters or special characters).");
        }

        
        String address;
        while (true) {
            System.out.print("Enter address: ");
            address = scanner.nextLine().trim();
            if (address.length() >= 10 && address.matches(".*[A-Za-z].*") && address.matches(".*\\d.*")) break;
            System.out.println("Invalid address. Address must contain at least 10 characters, including letters and numbers.");
        }

        
        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim();
            if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|in|edu|org|net)$")) break;
            System.out.println("Invalid email. Please enter a valid email address (e.g., user@example.com).");
        }

        Vector<Room> bookedRooms = new Vector<>();
        double totalAmount = 0;

        while (true) {
            System.out.println("\nAvailable rooms:");
            for (Room room : rooms) {
                if (room.isAvailable()) {
                    System.out.println(room);
                }
            }
            System.out.print("Enter room number to book (or 0 to finish): ");
            int roomNumber = scanner.nextInt();
            if (roomNumber == 0) break;

            Room room = null;
            for (Room r : rooms) {
                if (r.getRoomNumber() == roomNumber && r.isAvailable()) {
                    room = r;
                    break;
                }
            }

            if (room != null) {
                bookedRooms.add(room);
                room.setAvailable(false);
                totalAmount += room.getPrice();
                System.out.println("Room " + roomNumber + " booked successfully.");
            } else {
                System.out.println("Room not available or invalid room number.");
            }
        }

        scanner.nextLine();

        if (!bookedRooms.isEmpty()) {
            bookings.add(new Booking(bookedRooms, new Customer(name, contact, address, email)));
            System.out.println("\nBooking Complete!");
            System.out.println("Total Amount: " + totalAmount);
        } else {
            System.out.println("No rooms booked.");
        }
    }
}

public class Hotel18033v2 {
    public static void main(String[] args) {
        SerenitySuitesHotelManagementSystem hms = new SerenitySuitesHotelManagementSystem();
        Scanner scanner = new Scanner(System.in);

        int choice;

        do {
            System.out.println("\n1. Book Room\n2. View Available Rooms\n3. Cancel Booking\n4. Search Booking by Customer\n5. List All Rooms\n6. Add Room\n7. Exit");
            System.out.print("Choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    hms.bookRoom();
                    break;
                case 2:
                    hms.viewAvailableRooms();
                    break;
                case 3:
                    hms.cancelBooking();
                    break;
                case 4:
                    hms.searchBookingByCustomer();
                    break;
                case 5:
                    hms.listAllRooms();
                    break;
                case 6:
                    hms.addRoom();
                    break;
                case 7:
                    System.out.println("Thank you for using Serenity Suites!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 7);

        scanner.close();
    }
}
