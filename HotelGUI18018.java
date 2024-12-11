import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

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

class HotelGUI18018 extends JFrame {
    private Vector<Room> rooms = new Vector<>();
    private Connection connection;

    
    public HotelGUI18018() {
        
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/serenity_suites", "root", "");
            initializeRooms();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        
        setTitle("Serenity Suites Hotel Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        
        JLabel hotelNameLabel = new JLabel("<html><span style='font-size:24px; color:blue;'>\uD83C\uDFE8 Serenity Suites Hotel \uD83C\uDFE8</span></html>", JLabel.CENTER);
        hotelNameLabel.setFont(new Font("Serif", Font.BOLD, 28));
        hotelNameLabel.setForeground(Color.BLUE);
        add(hotelNameLabel, BorderLayout.NORTH);

        
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new FlowLayout());

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
        btnExit.addActionListener(e -> System.exit(0)); // Exit the application

        setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bookRoom(JTextArea displayArea) {
        JDialog bookingDialog = new JDialog(this, "Book Room", true);
        bookingDialog.setSize(400, 350);
        bookingDialog.setLayout(new GridLayout(6, 2));

        
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

        bookingDialog.add(new JLabel("Name:"));
        bookingDialog.add(txtName);
        bookingDialog.add(new JLabel("Contact:"));
        bookingDialog.add(txtContact);
        bookingDialog.add(new JLabel("Address:"));
        bookingDialog.add(txtAddress);
        bookingDialog.add(new JLabel("Email:"));
        bookingDialog.add(txtEmail);
        bookingDialog.add(new JLabel("Select Room:"));
        bookingDialog.add(roomSelection);
        bookingDialog.add(btnBook);
        bookingDialog.add(btnCancel);

        btnCancel.addActionListener(e -> bookingDialog.dispose());
        btnBook.addActionListener(e -> {
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            String address = txtAddress.getText().trim();
            String email = txtEmail.getText().trim();
            String selectedRoom = (String) roomSelection.getSelectedItem();

            // Perform validations
            if (!ValidationUtils.validateName(name)) {
                JOptionPane.showMessageDialog(bookingDialog, "Invalid name. Enter in 'Firstname Lastname' format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtils.validateContact(contact)) {
                JOptionPane.showMessageDialog(bookingDialog, "Invalid contact. Enter a valid 10-digit number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtils.validateAddress(address)) {
                JOptionPane.showMessageDialog(bookingDialog, "Invalid address. It must contain at least one letter and one digit.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtils.validateEmail(email)) {
                JOptionPane.showMessageDialog(bookingDialog, "Invalid email. Enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedRoom == null) {
                JOptionPane.showMessageDialog(bookingDialog, "No room selected. Please choose a room.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            
            Room room = null;
            for (Room r : rooms) {
                if (selectedRoom.contains("Room " + r.getRoomNumber())) {
                    room = r;
                    break;
                }
            }

            if (room != null) {
                room.setAvailable(false);
                updateRoomAvailability(room.getRoomNumber(), false);
                saveBooking(room, new Customer(name, contact, address, email));
                displayArea.append("Room " + room.getRoomNumber() + " booked successfully for " + name + ".\n");
                bookingDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(bookingDialog, "Room booking failed. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bookingDialog.setVisible(true);
    }

    private void updateRoomAvailability(int roomNumber, boolean available) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE rooms SET available = ? WHERE room_number = ?")) {
            stmt.setBoolean(1, available);
            stmt.setInt(2, roomNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating room availability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBooking(Room room, Customer customer) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bookings (customer_name, contact, address, email, rooms) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getContact());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, String.valueOf(room.getRoomNumber()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
}
