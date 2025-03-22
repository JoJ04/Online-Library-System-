package com.mycompany.online_library_management_system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BorrowManager extends JFrame {
    private JTextField bookTitleField;
    private JTextField bookAuthorField;
    private JTextField dueDateField;
    private JButton borrowButton;
    private String userEmail;
    private int bookId;
    public BorrowManager(String userEmail, int bookId, String bookTitle, String bookAuthor) {
        this.userEmail = userEmail;
        this.bookId = bookId;

        // Frame setup
        setTitle("Borrow Book");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        // Initialize components
        JLabel titleLabel = new JLabel("Book Title:");
        bookTitleField = new JTextField(bookTitle);
        bookTitleField.setEditable(false);

        JLabel authorLabel = new JLabel("Book Author:");
        bookAuthorField = new JTextField(bookAuthor);
        bookAuthorField.setEditable(false);

        JLabel dueDateLabel = new JLabel("Due Date (YYYY-MM-DD):");
        dueDateField = new JTextField(10);

        borrowButton = new JButton("Borrow Book");
        borrowButton.addActionListener(new BorrowBookAction());

        // Add components to the frame
        add(titleLabel);
        add(bookTitleField);
        add(authorLabel);
        add(bookAuthorField);
        add(dueDateLabel);
        add(dueDateField);
        add(new JLabel()); // Empty space
        add(borrowButton);

        // Center the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Action listener to handle borrowing the book
    private class BorrowBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String dueDateText = dueDateField.getText().trim();
            if (dueDateText.isEmpty()) {
                JOptionPane.showMessageDialog(BorrowManager.this, "Please enter a due date!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate the due date format and ensure it's in the future
            if (!dueDateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(BorrowManager.this, "Invalid due date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                java.time.LocalDate dueDate = java.time.LocalDate.parse(dueDateText);
                if (dueDate.isBefore(java.time.LocalDate.now())) {
                    JOptionPane.showMessageDialog(BorrowManager.this, "Due date must be in the future!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(BorrowManager.this, "Invalid date entered.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseManager.getConnection()) {
                // Verify the user's email and fetch their ID
                String checkUserQuery = "SELECT id FROM users WHERE email = ?";
                int userId;

                try (PreparedStatement userStmt = conn.prepareStatement(checkUserQuery)) {
                    userStmt.setString(1, userEmail);

                    try (ResultSet userRs = userStmt.executeQuery()) {
                        if (userRs.next()) {
                            userId = userRs.getInt("id");
                        } else {
                            JOptionPane.showMessageDialog(BorrowManager.this, "User email not found. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                // Check if the book is available
                String checkAvailabilityQuery = "SELECT num_of_books FROM books WHERE id = ?";
                try (PreparedStatement bookStmt = conn.prepareStatement(checkAvailabilityQuery)) {
                    bookStmt.setInt(1, bookId);

                    try (ResultSet bookRs = bookStmt.executeQuery()) {
                        if (bookRs.next()) {
                            int availableCopies = bookRs.getInt("num_of_books");
                            if (availableCopies <= 0) {
                                JOptionPane.showMessageDialog(BorrowManager.this, "Sorry, this book is not available right now!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // Proceed to borrow the book
                            borrowBook(conn, userId, dueDateText);
                        } else {
                            JOptionPane.showMessageDialog(BorrowManager.this, "Book not found in the database!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(BorrowManager.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

private void borrowBook(Connection conn, int userId, String dueDateText) throws SQLException {
    // Insert into the borrowings table
    String borrowQuery = "INSERT INTO borrowings (user_id, book_id, borrow_date, return_date, status) VALUES (?, ?, CURDATE(), ?, 'borrowed')";
    try (PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery)) {
        borrowStmt.setInt(1, userId);
        borrowStmt.setInt(2, bookId);
        borrowStmt.setString(3, dueDateText);
        borrowStmt.executeUpdate();
    }

    // Decrement the number of copies
    String decrementCopiesQuery = "UPDATE books SET num_of_books = num_of_books - 1 WHERE id = ? AND num_of_books > 0";
    try (PreparedStatement decrementStmt = conn.prepareStatement(decrementCopiesQuery)) {
        decrementStmt.setInt(1, bookId);
        decrementStmt.executeUpdate();
    }

    // Update the availability status based on the remaining copies
    String updateAvailabilityQuery = "UPDATE books SET availability = IF(num_of_books > 0, 1, 0) WHERE id = ?";
    try (PreparedStatement updateStmt = conn.prepareStatement(updateAvailabilityQuery)) {
        updateStmt.setInt(1, bookId);
        updateStmt.executeUpdate();
    }
    
    

    JOptionPane.showMessageDialog(BorrowManager.this, "Book borrowed successfully! Please return it by " + dueDateText);

    dispose();
}
    }
}

    