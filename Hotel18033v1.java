import java.util.Scanner;

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
    private Room[] rooms;
    private Customer customer;

    public Booking(Room[] rooms, Customer customer) {
        this.rooms = rooms;
        this.customer = customer;
    }

    public Room[] getRooms() {
        return rooms;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        StringBuilder details = new StringBuilder("Customer: " + customer + "\nRooms: ");
        for (Room room : rooms) {
            if (room != null) details.append(room.getRoomNumber()).append(" ");
        }
        return details.toString();
    }
}

class SerenitySuitesHotelManagementSystem {
    private Room[] rooms = new Room[15];
    private Booking[] bookings = new Booking[10];
    private int bookingCount = 0;
    private Scanner scanner = new Scanner(System.in);

    public SerenitySuitesHotelManagementSystem() {
        for (int i = 0; i < rooms.length; i++) {
            int roomNumber = 101 + i;
            String type = (roomNumber % 3 == 0) ? "Suite" : (roomNumber % 2 == 0) ? "Double" : "Single";
            rooms[i] = new Room(roomNumber, type);
        }
    }

    
    private String getValidName() {
        String name;
        while (true) {
            System.out.print("Enter customer name: ");
            name = scanner.nextLine().trim();
            if (name.matches("^[A-Z][a-z]+( [A-Z][a-z]+)+$")) {
                break;
            } else {
                System.out.println("Invalid name. Please enter a valid name (e.g., John Doe). Each word should start with a capital letter and contain alphabetic characters only.");
            }
        }
        return name;
    }

    
    private String getValidContact() {
        String contact;
        while (true) {
            System.out.print("Enter contact (10-digit number): ");
            contact = scanner.nextLine().trim();
            if (contact.matches("\\d{10}")) break;
            System.out.println("Invalid contact number. Please enter exactly 10 digits (no letters or special characters).");
        }
        return contact;
    }

    
    private String getValidAddress() {
        String address;
        while (true) {
            System.out.print("Enter address: ");
            address = scanner.nextLine().trim();
            if (address.length() >= 10 && address.matches(".*[A-Za-z].*") && address.matches(".*\\d.*")) break;
            System.out.println("Invalid address. Address must contain at least 10 characters, including letters and numbers.");
        }
        return address;
    }

    
    private String getValidEmail() {
        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim();
            if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|in|edu|org|net)$")) break;
            System.out.println("Invalid email. Please enter a valid email address (e.g., user@example.com).");
        }
        return email;
    }

    public void bookRoom() {
        // Get Valid Customer Details
        String name = getValidName();
        String contact = getValidContact();
        String address = getValidAddress();
        String email = getValidEmail();

        System.out.println("Available rooms:");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.println(room);
            }
        }

        Room[] bookedRooms = new Room[5];
        double totalAmount = 0;
        int roomCount = 0;

        while (true) {
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
                bookedRooms[roomCount++] = room;
                room.setAvailable(false);
                totalAmount += room.getPrice();
                System.out.println("Room " + roomNumber + " booked successfully.");
            } else {
                System.out.println("Room not available or invalid room number.");
            }
        }

        scanner.nextLine();

        if (roomCount > 0) {
            bookings[bookingCount++] = new Booking(bookedRooms, new Customer(name, contact, address, email));
            System.out.println("\nBooking Complete!");
            System.out.println("Total Amount: " + totalAmount);
        } else {
            System.out.println("No rooms booked.");
        }
    }

    public void viewAvailableRooms() {
        System.out.println("Available rooms:");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.println(room);
            }
        }
    }

    public void cancelBooking() {
        System.out.print("Enter customer name to cancel booking: ");
        String name = scanner.nextLine();

        for (int i = 0; i < bookingCount; i++) {
            if (bookings[i] != null && bookings[i].getCustomer().getName().equalsIgnoreCase(name)) {
                for (Room room : bookings[i].getRooms()) {
                    if (room != null) room.setAvailable(true);
                }
                System.out.println("Booking canceled for customer: " + name);
                for (int j = i; j < bookingCount - 1; j++) {
                    bookings[j] = bookings[j + 1];
                }
                bookings[--bookingCount] = null;
                return;
            }
        }
        System.out.println("Booking not found.");
    }

    public void searchBookingByCustomer() {
        System.out.print("Enter customer name to search: ");
        String name = scanner.nextLine();

        for (Booking booking : bookings) {
            if (booking != null && booking.getCustomer().getName().equalsIgnoreCase(name)) {
                System.out.println("Booking found:");
                System.out.println(booking);
                return;
            }
        }
        System.out.println("No booking found for the given customer.");
    }

    public void listAllRooms() {
        System.out.println("All rooms:");
        for (Room room : rooms) {
            System.out.println(room);
        }
    }
}

public class Hotel18033v1 {
    public static void main(String[] args) {
        SerenitySuitesHotelManagementSystem hms = new SerenitySuitesHotelManagementSystem();
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n1. Book Room\n2. View Available Rooms\n3. Cancel Booking\n4. Search Booking by Customer\n5. List All Rooms\n6. Exit");
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
                    System.out.println("Thank you for using Serenity Suites!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 6);

        scanner.close();
    }
}
