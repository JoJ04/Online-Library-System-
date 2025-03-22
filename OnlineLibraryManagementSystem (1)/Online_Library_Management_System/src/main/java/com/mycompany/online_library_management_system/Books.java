package com.mycompany.online_library_management_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Books extends JFrame {
    private JTable bookTable, borrowHistoryTable;
    private JTextField titleField, authorField;
    private JComboBox<String> genreComboBox, authorComboBox, yearComboBox;
    private JButton searchButton, borrowButton, clearNotificationsButton, filterButton;
    private String userEmail;
    private JTextArea notificationArea;

    public Books(String userEmail) {
    this.userEmail = userEmail;

    // Frame setup
    setTitle("Library Book Browser");
    setSize(900, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // Panel for the books table
    JPanel booksPanel = new JPanel(new BorderLayout());
    JPanel searchPanel = new JPanel(new GridLayout(2, 4, 10, 10));
    
    // First Row: Search Fields
    JLabel titleLabel = new JLabel("Title:");
    titleField = new JTextField();
    JLabel authorLabel = new JLabel("Author:");
    authorField = new JTextField();
    searchButton = new JButton("Search");
    borrowButton = new JButton("Borrow Book");

    searchPanel.add(titleLabel);
    searchPanel.add(titleField);
    searchPanel.add(authorLabel);
    searchPanel.add(authorField);
    searchPanel.add(searchButton);
    searchPanel.add(borrowButton);
    searchPanel.add(new JLabel()); // Spacer
    searchPanel.add(new JLabel()); // Spacer

    // Second Row: Combo Boxes for Categorization
    JLabel genreLabel = new JLabel("Genre:");
    genreComboBox = new JComboBox<>();
    JLabel yearLabel = new JLabel("Publication Year:");
    yearComboBox = new JComboBox<>();
    JLabel authorFilterLabel = new JLabel("Author:");
    authorComboBox = new JComboBox<>();
    filterButton = new JButton("Filter");

    searchPanel.add(genreLabel);
    searchPanel.add(genreComboBox);
    searchPanel.add(authorFilterLabel);
    searchPanel.add(authorComboBox);
    searchPanel.add(yearLabel);
    searchPanel.add(yearComboBox);
    searchPanel.add(new JLabel()); // Spacer
    searchPanel.add(filterButton);

    booksPanel.add(searchPanel, BorderLayout.NORTH);

    // Books Table
    bookTable = new JTable();
    JScrollPane bookScrollPane = new JScrollPane(bookTable);
    booksPanel.add(bookScrollPane, BorderLayout.CENTER);

    // Panel for borrow history table
    JPanel borrowHistoryPanel = new JPanel(new BorderLayout());
    borrowHistoryTable = new JTable();
    JScrollPane borrowHistoryScrollPane = new JScrollPane(borrowHistoryTable);
    borrowHistoryPanel.add(new JLabel("Borrow History", JLabel.CENTER), BorderLayout.NORTH);
    borrowHistoryPanel.add(borrowHistoryScrollPane, BorderLayout.CENTER);

    // Main panel to stack books and borrow history tables
    JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
    mainPanel.add(booksPanel);
    mainPanel.add(borrowHistoryPanel);

    add(mainPanel, BorderLayout.CENTER);

    // Notification Area
    notificationArea = new JTextArea();
    notificationArea.setEditable(false);
    JScrollPane notificationScrollPane = new JScrollPane(notificationArea);
    notificationScrollPane.setBorder(BorderFactory.createTitledBorder("Notifications"));

    // Clear Notifications Button
    clearNotificationsButton = new JButton("Clear Notifications");
    clearNotificationsButton.addActionListener(new ClearNotificationsAction());

    JPanel notificationPanel = new JPanel(new BorderLayout());
    notificationPanel.add(notificationScrollPane, BorderLayout.CENTER);
    notificationPanel.add(clearNotificationsButton, BorderLayout.SOUTH);

    add(notificationPanel, BorderLayout.EAST);

    // Load initial data
    loadComboBoxData(); // Load data for combo boxes
    loadBooks("", ""); 
    loadBorrowHistory(); 
    loadNotifications(); // Load notifications

    // Add action listeners
    searchButton.addActionListener(new SearchAction());
    borrowButton.addActionListener(new BorrowAction());
    filterButton.addActionListener(new FilterAction());

    setLocationRelativeTo(null); // Center the frame
    setVisible(true);
}


    private void loadBooks(String title, String author) {
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre","Available Copies", "Availability"}, 0);

        String query = "SELECT id, title, author, genre, num_of_books, availability FROM books WHERE title LIKE ? AND author LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + title + "%");
            stmt.setString(2, "%" + author + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getInt("num_of_books"),
                            rs.getInt("availability") == 1 ? "Yes" : "No"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }

        bookTable.setModel(tableModel);
    }

    private void loadComboBoxData() {
        // Load Genre
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT genre FROM books")) {

            genreComboBox.addItem("All");
            while (rs.next()) {
                genreComboBox.addItem(rs.getString("genre"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading genres: " + e.getMessage());
        }

        // Load Authors
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT author FROM books")) {

            authorComboBox.addItem("All");
            while (rs.next()) {
                authorComboBox.addItem(rs.getString("author"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading authors: " + e.getMessage());
        }

        // Load Publication Years
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT YEAR(publication_date) AS year FROM books")) {

            yearComboBox.addItem("All");
            while (rs.next()) {
                yearComboBox.addItem(rs.getString("year"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading publication years: " + e.getMessage());
        }
    }

    private class FilterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String genre = genreComboBox.getSelectedItem().toString();
            String author = authorComboBox.getSelectedItem().toString();
            String year = yearComboBox.getSelectedItem().toString();

            DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "Available Copies", "Availability"}, 0);

            String query = "SELECT id, title, author, genre, num_of_books, availability FROM books WHERE 1=1";
            if (!genre.equals("All")) query += " AND genre = '" + genre + "'";
            if (!author.equals("All")) query += " AND author = '" + author + "'";
            if (!year.equals("All")) query += " AND YEAR(publication_date) = '" + year + "'";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getInt("num_of_books"),
                            rs.getInt("availability") == 1 ? "Yes" : "No"
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(Books.this, "Error filtering books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            bookTable.setModel(tableModel);
        }
    }

    
    private void loadBorrowHistory() {
    DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Book Title", "Author", "Borrow Date", "Return Date", "Status", "Overdue Days"}, 0
    );

    String query = "SELECT br.id, b.title, b.author, br.borrow_date, br.return_date, br.status, " +
                   "DATEDIFF(CURDATE(), br.return_date) AS overdue_days " +
                   "FROM borrowings br " +
                   "JOIN books b ON br.book_id = b.id " +
                   "WHERE br.user_id = (SELECT id FROM users WHERE email = ?)";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, userEmail);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDate("borrow_date"),
                        rs.getDate("return_date"),
                        rs.getString("status"),
                        rs.getInt("overdue_days") > 0 ? rs.getInt("overdue_days") : 0
                });
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading borrow history: " + e.getMessage());
    }

    borrowHistoryTable.setModel(tableModel);
}

    private void loadNotifications() {
    notificationArea.setText(""); 

    addBorrowedBookNotifications();
    addOverdueNotifications();
}

private void addBorrowedBookNotifications() {
    String query = "SELECT b.title, br.return_date FROM borrowings br JOIN books b ON br.book_id = b.id " +
                   "WHERE br.user_id = (SELECT id FROM users WHERE email = ?) AND br.status = 'borrowed'";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, userEmail);

        try (ResultSet rs = stmt.executeQuery()) {
            notificationArea.append("Borrowed Books:\n");
            while (rs.next()) {
                notificationArea.append("Title: " + rs.getString("title") + "\n");
                notificationArea.append("Due Date: " + rs.getDate("return_date") + "\n\n");
            }
        }
    } catch (SQLException e) {
        notificationArea.append("Error fetching borrowed books: " + e.getMessage() + "\n");
    }
}

private void addOverdueNotifications() {
    String query = "SELECT b.title, u.email, br.return_date FROM borrowings br " +
                   "JOIN books b ON br.book_id = b.id " +
                   "JOIN users u ON br.user_id = u.id " +
                   "WHERE br.status = 'overdue'";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        notificationArea.append("Overdue Books:\n");
        while (rs.next()) {
            notificationArea.append("Title: " + rs.getString("title") + "\n");
            notificationArea.append("User Email: " + rs.getString("email") + "\n");
            notificationArea.append("Due Date: " + rs.getDate("return_date") + "\n\n");
        }
    } catch (SQLException e) {
        notificationArea.append("Error fetching overdue books: " + e.getMessage() + "\n");
    }
}

    private class ClearNotificationsAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        notificationArea.setText(""); // Clear the notification area
    }
}

    private class SearchAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        loadBooks(title, author);
    }
}
private class BorrowAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow(); // Get the selected row in the book table
        if (selectedRow != -1) {
            int bookId = (int) bookTable.getValueAt(selectedRow, 0); // Get book ID from the table
            String bookTitle = (String) bookTable.getValueAt(selectedRow, 1); // Get book title
            String bookAuthor = (String) bookTable.getValueAt(selectedRow, 2); // Get book author

           
            new BorrowManager(userEmail, bookId, bookTitle, bookAuthor).setVisible(true);
            loadBooks("", ""); 
            loadBorrowHistory(); 
        } 
        else {
            
            JOptionPane.showMessageDialog(Books.this, "Please select a book to borrow.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

   
}
