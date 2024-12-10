import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


class Room {
    private int roomNumber;
    private String type;
    private boolean available;

    public Room(int roomNumber, String type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.available = true; 
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
        return "Customer[name=" + name + ", contact=" + contact + ", address=" + address + ", email=" + email + "]";
    }
}


class DatabaseManager {
    private Connection connection;

    public DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/serenity_suites", "root", "");
    }

    public List<Room> fetchRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int roomNumber = rs.getInt("room_number");
                String type = rs.getString("type");
                boolean available = rs.getBoolean("available");
                Room room = new Room(roomNumber, type);
                room.setAvailable(available);
                rooms.add(room);
            }
        }
        return rooms;
    }

    public void updateRoomAvailability(int roomNumber, boolean available) throws SQLException {
        String query = "UPDATE rooms SET available = ? WHERE room_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, available);
            stmt.setInt(2, roomNumber);
            stmt.executeUpdate();
        }
    }

    public void saveBooking(Customer customer, Room room) throws SQLException {
        String query = "INSERT INTO bookings (customer_name, contact, address, email, rooms) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getContact());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getEmail());
            stmt.setInt(5, room.getRoomNumber());
            stmt.executeUpdate();
        }
    }
}


class ValidationUtils {
    public static boolean validateName(String name) {
        return name.matches("^[A-Z][a-z]+( [A-Z][a-z]+)+$");
    }

    public static boolean validateContact(String contact) {
        return contact.matches("\\d{10}");
    }

    public static boolean validateAddress(String address) {
        return address.length() >= 10 && address.matches(".*[A-Za-z].*") && address.matches(".*\\d.*");
    }

    public static boolean validateEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|in|edu|org|net)$");
    }
}


public class HotelGUI18018 extends JFrame {
    private List<Room> rooms;
    private DatabaseManager dbManager;

    public HotelGUI18018() {
        
        try {
            dbManager = new DatabaseManager();
            rooms = dbManager.fetchRooms();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error initializing application: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Serenity Suites Hotel Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        
        JLabel hotelNameLabel = new JLabel("<html><span style='font-size:24px; color:blue;'>🏨 Serenity Suites Hotel 🏨</span></html>", JLabel.CENTER);
        hotelNameLabel.setFont(new Font("Serif", Font.BOLD, 28));
        add(hotelNameLabel, BorderLayout.NORTH);

        
        JPanel menuPanel = new JPanel();
        JButton btnBookRoom = new JButton("Book Room");
        JButton btnViewAvailableRooms = new JButton("View Available Rooms");
        JButton btnListAllRooms = new JButton("List All Rooms");
        JButton btnExit = new JButton("Exit");

        menuPanel.add(btnBookRoom);
        menuPanel.add(btnViewAvailableRooms);
        menuPanel.add(btnListAllRooms);
        menuPanel.add(btnExit);
        add(menuPanel, BorderLayout.NORTH);

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.CENTER);

        
        btnBookRoom.addActionListener(e -> bookRoom(displayArea));
        btnViewAvailableRooms.addActionListener(e -> viewAvailableRooms(displayArea));
        btnListAllRooms.addActionListener(e -> listAllRooms(displayArea));
        btnExit.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void bookRoom(JTextArea displayArea) {
        JDialog dialog = new JDialog(this, "Book Room", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(6, 2));

        JTextField txtName = new JTextField();
        JTextField txtContact = new JTextField();
        JTextField txtAddress = new JTextField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> roomSelection = new JComboBox<>();

        for (Room room : rooms) {
            if (room.isAvailable()) {
                roomSelection.addItem("Room " + room.getRoomNumber() + " (" + room.getType() + ")");
            }
        }

        JButton btnBook = new JButton("Book");
        JButton btnCancel = new JButton("Cancel");

        dialog.add(new JLabel("Name:"));
        dialog.add(txtName);
        dialog.add(new JLabel("Contact:"));
        dialog.add(txtContact);
        dialog.add(new JLabel("Address:"));
        dialog.add(txtAddress);
        dialog.add(new JLabel("Email:"));
        dialog.add(txtEmail);
        dialog.add(new JLabel("Select Room:"));
        dialog.add(roomSelection);
        dialog.add(btnBook);
        dialog.add(btnCancel);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnBook.addActionListener(e -> {
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            String address = txtAddress.getText().trim();
            String email = txtEmail.getText().trim();

            if (!ValidationUtils.validateName(name) || !ValidationUtils.validateContact(contact)
                    || !ValidationUtils.validateAddress(address) || !ValidationUtils.validateEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Invalid inputs. Please correct them.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedRoom = (String) roomSelection.getSelectedItem();
            Room room = rooms.stream().filter(r -> selectedRoom.contains("Room " + r.getRoomNumber())).findFirst().orElse(null);

            if (room != null) {
                try {
                    room.setAvailable(false);
                    dbManager.updateRoomAvailability(room.getRoomNumber(), false);
                    dbManager.saveBooking(new Customer(name, contact, address, email), room);
                    displayArea.append("Room " + room.getRoomNumber() + " booked successfully for " + name + ".\n");
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error booking room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }

    private void viewAvailableRooms(JTextArea displayArea) {
        displayArea.setText("Available Rooms:\n");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                displayArea.append(room + "\n");
            }
        }
    }

    private void listAllRooms(JTextArea displayArea) {
        displayArea.setText("All Rooms:\n");
        for (Room room : rooms) {
            displayArea.append(room + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HotelGUI18018::new);
    }
}
