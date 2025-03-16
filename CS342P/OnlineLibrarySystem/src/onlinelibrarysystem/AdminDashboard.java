/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package onlinelibrarysystem;

/**
 *
 * @author gogo-
 */
import javax.swing.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JLabel label = new JLabel("Welcome, Admin!", JLabel.CENTER);
        add(label);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}