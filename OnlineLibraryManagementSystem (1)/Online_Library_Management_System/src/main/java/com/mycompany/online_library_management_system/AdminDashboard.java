package com.mycompany.online_library_management_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private JTable bookTable, borrowTable;
    private JTextArea notificationArea;
    private JButton refreshBooksButton, refreshBorrowsButton, addBookButton, editBookButton, deleteBookButton, toggleAvailabilityButton, manageUserButton,clearnotfication;
    private JButton generateReportButton, generateReceiptButton, markAsReturnedButton;

    public AdminDashboard() {
        // Frame setup
        setTitle("Library Admin Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Book Management Panel
        JPanel bookPanel = new JPanel(new BorderLayout(10, 10));
        bookTable = new JTable();
        JScrollPane bookScrollPane = new JScrollPane(bookTable);

        JPanel bookButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        refreshBooksButton = new JButton("Refresh Books");
        addBookButton = new JButton("Add Book");
        editBookButton = new JButton("Edit Book");
        deleteBookButton = new JButton("Delete Book");
        toggleAvailabilityButton = new JButton("Toggle Availability");
        bookButtonsPanel.add(refreshBooksButton);
        bookButtonsPanel.add(addBookButton);
        bookButtonsPanel.add(editBookButton);
        bookButtonsPanel.add(deleteBookButton);
        bookButtonsPanel.add(toggleAvailabilityButton);


        bookPanel.add(new JLabel("Book Inventory", JLabel.CENTER), BorderLayout.NORTH);
        bookPanel.add(bookScrollPane, BorderLayout.CENTER);
        bookPanel.add(bookButtonsPanel, BorderLayout.SOUTH);

        // Borrowing History Panel
        JPanel borrowPanel = new JPanel(new BorderLayout(10, 10));
        borrowTable = new JTable();
        JScrollPane borrowScrollPane = new JScrollPane(borrowTable);

        JPanel borrowButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        refreshBorrowsButton = new JButton("Refresh Borrows");
        generateReportButton = new JButton("Generate Report");
        generateReceiptButton = new JButton("Generate Receipt");
        markAsReturnedButton = new JButton("Mark as Returned");
        manageUserButton = new JButton("Manage User Info");
        clearnotfication =new JButton("clear notification Panel");

        borrowButtonsPanel.add(refreshBorrowsButton);
        borrowButtonsPanel.add(generateReportButton);
        borrowButtonsPanel.add(generateReceiptButton);
        borrowButtonsPanel.add(markAsReturnedButton);
        borrowButtonsPanel.add(manageUserButton);
        borrowButtonsPanel.add(clearnotfication);


        borrowPanel.add(new JLabel("Borrowing History", JLabel.CENTER), BorderLayout.NORTH);
        borrowPanel.add(borrowScrollPane, BorderLayout.CENTER);
        borrowPanel.add(borrowButtonsPanel, BorderLayout.SOUTH);

        // Notification Area
        JPanel notificationPanel = new JPanel(new BorderLayout(10, 10));
        notificationArea = new JTextArea(10, 30);
        notificationArea.setEditable(false);
        JScrollPane notificationScrollPane = new JScrollPane(notificationArea);
        notificationPanel.add(new JLabel("Notifications", JLabel.CENTER), BorderLayout.NORTH);
        notificationPanel.add(notificationScrollPane, BorderLayout.CENTER);

        // Add panels to frame
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bookPanel, borrowPanel);
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);
        add(notificationPanel, BorderLayout.EAST);

        // Add action listeners
        refreshBooksButton.addActionListener(new RefreshBooksAction());
        addBookButton.addActionListener(new AddBookAction());
        editBookButton.addActionListener(new EditBookAction());
        deleteBookButton.addActionListener(new DeleteBookAction());
        toggleAvailabilityButton.addActionListener(new ToggleAvailabilityAction());
        refreshBorrowsButton.addActionListener(new RefreshBorrowsAction());
        generateReportButton.addActionListener(new GenerateReportAction());
        generateReceiptButton.addActionListener(new GenerateReceiptAction());
        markAsReturnedButton.addActionListener(new MarkAsReturnedAction());
        manageUserButton.addActionListener(new ManageUserAction());
        clearnotfication.addActionListener(new clearnotfication());
        loadBooks();
        loadBorrows();
        loadNotifications();

        setLocationRelativeTo(null);
        setVisible(true);
    }

     class ManageUserAction implements ActionListener{

          public void actionPerformed(ActionEvent e){
          
              
          new MemberManagement();
          
          }
         
     }
    private void loadBooks() {
        
          
        DefaultTableModel bookModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "Genre", "Copies", "Availability"}, 0
        );

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            
            while (rs.next()) {
                bookModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("num_of_books"),
                    rs.getInt("availability") == 1 ? "Yes" : "No"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        bookTable.setModel(bookModel);
    }
    private void updateOverdueStatus() {
        
        String query = "UPDATE borrowings SET status = 'overdue' WHERE status = 'borrowed' AND return_date < CURDATE()";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate(); // Update overdue status
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating overdue status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBorrows() {
        updateOverdueStatus();
        DefaultTableModel borrowModel = new DefaultTableModel(
            new String[]{"ID", "User ID", "Book ID", "Borrow Date", "Return Date", "Status"}, 0
        );

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM borrowings")) {

            while (rs.next()) {
                borrowModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("book_id"),
                    rs.getDate("borrow_date"),
                    rs.getDate("return_date"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        borrowTable.setModel(borrowModel);
    }

    
    
    private class RefreshBooksAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadBooks();
        }
    }

    private class AddBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = JOptionPane.showInputDialog("Enter Book Title:");
            String author = JOptionPane.showInputDialog("Enter Book Author:");
            String genre = JOptionPane.showInputDialog("Enter Book Genre:");
            String copies = JOptionPane.showInputDialog("Enter Number of Copies:");

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO books (title, author, genre, num_of_books, availability) VALUES (?, ?, ?, ?, ?)")) {

                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.setString(3, genre);
                stmt.setInt(4, Integer.parseInt(copies));
                stmt.setInt(5, Integer.parseInt(copies) > 0 ? 1 : 0);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(AdminDashboard.this, "Book added successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error adding book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class EditBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a book to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int bookId = (int) bookTable.getValueAt(selectedRow, 0);
            String title = JOptionPane.showInputDialog("Enter New Title:", bookTable.getValueAt(selectedRow, 1));
            String author = JOptionPane.showInputDialog("Enter New Author:", bookTable.getValueAt(selectedRow, 2));
            String genre = JOptionPane.showInputDialog("Enter New Genre:", bookTable.getValueAt(selectedRow, 3));
            String copies = JOptionPane.showInputDialog("Enter New Number of Copies:", bookTable.getValueAt(selectedRow, 4));

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE books SET title = ?, author = ?, genre = ?, num_of_books = ?, availability = ? WHERE id = ?")) {

                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.setString(3, genre);
                stmt.setInt(4, Integer.parseInt(copies));
                stmt.setInt(5, Integer.parseInt(copies) > 0 ? 1 : 0);
                stmt.setInt(6, bookId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(AdminDashboard.this, "Book updated successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error updating book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DeleteBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a book to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int bookId = (int) bookTable.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {

                stmt.setInt(1, bookId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(AdminDashboard.this, "Book deleted successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error deleting book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ToggleAvailabilityAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a book to toggle availability.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int bookId = (int) bookTable.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE books SET availability = NOT availability WHERE id = ?")) {

                stmt.setInt(1, bookId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(AdminDashboard.this, "Book availability toggled successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error toggling availability: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RefreshBorrowsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadBorrows();
        }
    }

    private class GenerateReportAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String reportContent = "Library Report\n\n";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Total Borrowings
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_borrowings FROM borrowings")) {
                if (rs.next()) {
                    reportContent += "Total Borrowings: " + rs.getInt("total_borrowings") + "\n";
                }
            }

            // Total Books
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_books FROM books")) {
                if (rs.next()) {
                    reportContent += "Total Books: " + rs.getInt("total_books") + "\n";
                }
            }

            // Total Users
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_users FROM users")) {
                if (rs.next()) {
                    reportContent += "Total Users: " + rs.getInt("total_users") + "\n";
                }
            }

            // Most Borrowed Book
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT b.title, COUNT(br.book_id) AS borrow_count " +
                    "FROM borrowings br " +
                    "JOIN books b ON br.book_id = b.id " +
                    "GROUP BY br.book_id " +
                    "ORDER BY borrow_count DESC " +
                    "LIMIT 1")) {
                if (rs.next()) {
                    reportContent += "\nMost Borrowed Book:\n";
                    reportContent += "Title: " + rs.getString("title") + "\n";
                    reportContent += "Borrow Count: " + rs.getInt("borrow_count") + "\n";
                }
            }

            // Write the report to a file
            try (FileWriter writer = new FileWriter("LibraryReport.txt")) {
                writer.write(reportContent);
                JOptionPane.showMessageDialog(AdminDashboard.this, "Report generated successfully as LibraryReport.txt!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error writing report file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(AdminDashboard.this, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private void loadNotifications() {
    

    String overdueQuery = "SELECT u.email, b.title, br.return_date " +
                          "FROM borrowings br " +
                          "JOIN books b ON br.book_id = b.id " +
                          "JOIN users u ON br.user_id = u.id " +
                          "WHERE br.status = 'Overdue'";

    String newBorrowQuery = "SELECT u.email, b.title, br.return_date " +
                            "FROM borrowings br " +
                            "JOIN books b ON br.book_id = b.id " +
                            "JOIN users u ON br.user_id = u.id " +
                            "WHERE br.borrow_date = CURDATE()";

    try (Connection conn = DatabaseManager.getConnection()) {
        // Handle overdue books
        try (PreparedStatement stmt = conn.prepareStatement(overdueQuery);
             ResultSet rs = stmt.executeQuery()) {
            notificationArea.append("Overdue Books:\n");
            while (rs.next()) {
                notificationArea.append("User Email: " + rs.getString("email") +
                        ", Title: " + rs.getString("title") +
                        ", Due Date: " + rs.getDate("return_date") + "\n");
            }
        }

        // Handle new borrowings
        try (PreparedStatement stmt = conn.prepareStatement(newBorrowQuery);
             ResultSet rs = stmt.executeQuery()) {
            notificationArea.append("\nNew Borrowings:\n");
            while (rs.next()) {
                notificationArea.append("User Email: " + rs.getString("email") +
                        ", Title: " + rs.getString("title") +
                        ", Due Date: " + rs.getDate("return_date") + "\n");
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}



    private class GenerateReceiptAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = borrowTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a rental to generate a receipt.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int rentalId = (int) borrowTable.getValueAt(selectedRow, 0);
            int userId = (int) borrowTable.getValueAt(selectedRow, 1);
            int bookId = (int) borrowTable.getValueAt(selectedRow, 2);
            String borrowDate = borrowTable.getValueAt(selectedRow, 3).toString();
            String returnDate = borrowTable.getValueAt(selectedRow, 4).toString();
            String status = (String) borrowTable.getValueAt(selectedRow, 5);

            String receiptContent = "Library Borrowing Receipt\n"
                    + "--------------------------\n"
                    + "Rental ID: " + rentalId + "\n"
                    + "User ID: " + userId + "\n"
                    + "Book ID: " + bookId + "\n"
                    + "Borrow Date: " + borrowDate + "\n"
                    + "Return Date: " + returnDate + "\n"
                    + "Status: " + status + "\n";

            try (FileWriter writer = new FileWriter("Receipt_" + rentalId + ".txt")) {
                writer.write(receiptContent);
                JOptionPane.showMessageDialog(AdminDashboard.this, "Receipt saved as Receipt_" + rentalId + ".txt");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error saving receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class MarkAsReturnedAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(AdminDashboard.this, "Please select a rental to mark as returned.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int borrowId = (int) borrowTable.getValueAt(selectedRow, 0); 
        int bookId = (int) borrowTable.getValueAt(selectedRow, 2);   

        try (Connection conn = DatabaseManager.getConnection()) {
            // Update the borrow status to "returned"
            String updateBorrowQuery = "UPDATE borrowings SET status = 'returned' WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateBorrowQuery)) {
                stmt.setInt(1, borrowId);
                stmt.executeUpdate();
            }

            // Increment the number of available copies and update availability
            String updateBookQuery = "UPDATE books SET num_of_books = num_of_books + 1, " +
                                      "availability = IF(num_of_books + 1 > 0, 1, 0) WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookQuery)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(AdminDashboard.this, "Book marked as returned!");
            loadBorrows();
            loadBooks();   
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(AdminDashboard.this, "Error marking book as returned: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    class clearnotfication implements ActionListener{

          public void actionPerformed(ActionEvent e){
          
              
          notificationArea.setText("");
          
          }
         
     }
}

