import java.sql.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.*;

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

    public String getContact() {
        return contact;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
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
    private Scanner scanner = new Scanner(System.in);
    private Connection connection;

    public SerenitySuitesHotelManagementSystem() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/serenity_suites", "root", "");
            initializeRooms();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private void initializeRooms() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                int roomNumber = rs.getInt("room_number");
                String type = rs.getString("type");
                boolean available = rs.getBoolean("available");
                Room room = new Room(roomNumber, type);
                room.setAvailable(available);
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    public void bookRoom() {
        String name = promptForValidName();
        String contact = promptForValidContact();
        String address = promptForValidAddress();
        String email = promptForValidEmail();

        Vector<Room> bookedRooms = getSelectedRooms();
        if (bookedRooms.isEmpty()) {
            System.out.println("No valid rooms selected. Booking aborted.");
            return;
        }

        double totalAmount = 0;
        for (Room room : bookedRooms) {
            room.setAvailable(false);
            totalAmount += room.getPrice();
        }

        saveBooking(bookedRooms, new Customer(name, contact, address, email));

        System.out.println("Booking Complete! Total Amount: " + totalAmount);
    }

    private void saveBooking(Vector<Room> rooms, Customer customer) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bookings (customer_name, contact, address, email, rooms) VALUES (?, ?, ?, ?, ?)")) {
            StringBuilder roomNumbers = new StringBuilder();
            for (Room room : rooms) {
                roomNumbers.append(room.getRoomNumber()).append(",");
            }

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getContact());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, roomNumbers.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving booking: " + e.getMessage());
        }
    }

    private Vector<Room> getSelectedRooms() {
        System.out.println("Available rooms:");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.println(room);
            }
        }

        Vector<Room> selectedRooms = new Vector<>();
        System.out.println("Enter room numbers one by one to book (Enter 0 to finish):");

        while (true) {
            System.out.print("Enter room number: ");
            int roomNumber = scanner.nextInt();
            if (roomNumber == 0) {
                break;
            }

            Room room = findRoomByNumber(roomNumber);
            if (room != null && room.isAvailable()) {
                selectedRooms.add(room);
                System.out.println("Room " + roomNumber + " added to booking.");
            } else {
                System.out.println("Room " + roomNumber + " is not available or does not exist.");
            }
        }

        return selectedRooms;
    }

    private Room findRoomByNumber(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    private String promptForValidName() {
        String name;
        while (true) {
            System.out.print("Enter customer name (First Last): ");
            name = scanner.nextLine();
            if (validateName(name)) {
                break;
            } else {
                System.out.println("Invalid name. Please enter a valid name in 'First Last' format.");
            }
        }
        return name;
    }

    private String promptForValidContact() {
        String contact;
        while (true) {
            System.out.print("Enter contact: ");
            contact = scanner.nextLine();
            if (validateContact(contact)) {
                break;
            } else {
                System.out.println("Invalid contact number. Please enter a 10-digit contact number.");
            }
        }
        return contact;
    }

    private String promptForValidAddress() {
        String address;
        while (true) {
            System.out.print("Enter address: ");
            address = scanner.nextLine();
            if (validateAddress(address)) {
                break;
            } else {
                System.out.println("Address must be at least 10 characters long and contain both letters and numbers.");
            }
        }
        return address;
    }

    private String promptForValidEmail() {
        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = scanner.nextLine();
            if (validateEmail(email)) {
                break;
            } else {
                System.out.println("Invalid email format. Please enter a valid email address.");
            }
        }
        return email;
    }

    private boolean validateName(String name) {
        return name.matches("^[A-Z][a-z]{1,}[ ]+[A-Z][a-z]{1,}$");
    }

    private boolean validateContact(String contact) {
        return contact.matches("[0-9]{10}");
    }

    private boolean validateAddress(String address) {
        return address.length() >= 10 && address.matches(".*[A-Za-z].*") && address.matches(".*[0-9].*");
    }

    private boolean validateEmail(String email) {
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    public void viewAvailableRooms() {
        System.out.println("Available rooms:");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.println(room);
            }
        }
    }
}

public class Hotel18018v3 {
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
                    // Implement cancelBooking() here
                    break;
                case 4:
                    // Implement searchBookingByCustomer() here
                    break;
                case 5:
                    // List all rooms
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
