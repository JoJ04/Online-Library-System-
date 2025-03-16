/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class OnlineLibrarySystem {

    private static Connection connectToDatabase() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/library_management", "root", "MySQLJory2022");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Online Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(180, 80, 80, 25);
        panel.add(registerButton);

        JButton searchBooksButton = new JButton("Search Books");
        searchBooksButton.setBounds(100, 120, 150, 25);
        panel.add(searchBooksButton);

        JLabel messageLabel = new JLabel("");
        messageLabel.setBounds(10, 160, 300, 25);
        panel.add(messageLabel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("Fields cannot be empty.");
                    return;
                }

                try (Connection conn = connectToDatabase()) {
                    if (conn != null) {
                        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                        ps.setString(1, username);
                        ps.setString(2, password);
                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {
                            messageLabel.setText("Login successful!");
                        } else {
                            messageLabel.setText("Invalid credentials.");
                        }
                    }
                } catch (SQLException ex) {
                    messageLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("Fields cannot be empty.");
                    return;
                }

                try (Connection conn = connectToDatabase()) {
                    if (conn != null) {
                        PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                        ps.setString(1, username);
                        ps.setString(2, password);
                        ps.executeUpdate();

                        messageLabel.setText("Registration successful!");
                    }
                } catch (SQLException ex) {
                    messageLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        searchBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame searchFrame = new JFrame("Search Books");
                searchFrame.setSize(500, 300);

                JPanel searchPanel = new JPanel();
                searchPanel.setLayout(null);

                JLabel titleLabel = new JLabel("Title:");
                titleLabel.setBounds(10, 20, 80, 25);
                searchPanel.add(titleLabel);

                JTextField titleText = new JTextField(20);
                titleText.setBounds(100, 20, 165, 25);
                searchPanel.add(titleText);

                JLabel authorLabel = new JLabel("Author:");
                authorLabel.setBounds(10, 50, 80, 25);
                searchPanel.add(authorLabel);

                JTextField authorText = new JTextField(20);
                authorText.setBounds(100, 50, 165, 25);
                searchPanel.add(authorText);

                JButton searchButton = new JButton("Search");
                searchButton.setBounds(10, 80, 100, 25);
                searchPanel.add(searchButton);

                JTextArea resultArea = new JTextArea();
                resultArea.setBounds(10, 120, 460, 120);
                resultArea.setEditable(false);
                searchPanel.add(resultArea);

                searchButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String title = titleText.getText();
                        String author = authorText.getText();

                        try (Connection conn = connectToDatabase()) {
                            if (conn != null) {
                                String query = "SELECT * FROM books WHERE title LIKE ? AND author LIKE ?";
                                PreparedStatement ps = conn.prepareStatement(query);
                                ps.setString(1, "%" + title + "%");
                                ps.setString(2, "%" + author + "%");
                                ResultSet rs = ps.executeQuery();

                                StringBuilder results = new StringBuilder();
                                while (rs.next()) {
                                    results.append("Title: ").append(rs.getString("title"))
                                            .append(", Author: ").append(rs.getString("author"))
                                            .append(", Genre: ").append(rs.getString("genre"))
                                            .append("\n");
                                }

                                resultArea.setText(results.toString());
                            }
                        } catch (SQLException ex) {
                            resultArea.setText("Error: " + ex.getMessage());
                        }
                    }
                });

                searchFrame.add(searchPanel);
                searchFrame.setVisible(true);
            }
        });
    }
}
