package com.mycompany.online_library_management_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MemberManagement extends JFrame {
    private JTable memberTable;
    private JButton refreshButton, addMemberButton, editMemberButton, deleteMemberButton;

    public MemberManagement() {
        // Frame setup
        setTitle("Member Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table for members
        memberTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(memberTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("Refresh Members");
        addMemberButton = new JButton("Add Member");
        editMemberButton = new JButton("Edit Member");
        deleteMemberButton = new JButton("Delete Member");

        buttonPanel.add(refreshButton);
        buttonPanel.add(addMemberButton);
        buttonPanel.add(editMemberButton);
        buttonPanel.add(deleteMemberButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        refreshButton.addActionListener(new RefreshMembersAction());
        addMemberButton.addActionListener(new AddMemberAction());
        editMemberButton.addActionListener(new EditMemberAction());
        deleteMemberButton.addActionListener(new DeleteMemberAction());

        // Load members initially
        loadMembers();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadMembers() {
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Email"}, 0);

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, email FROM users")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("email")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        memberTable.setModel(tableModel);
    }

    private class RefreshMembersAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadMembers();
        }
    }

    private class AddMemberAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = JOptionPane.showInputDialog(MemberManagement.this, "Enter Member Email:");
            String password = JOptionPane.showInputDialog(MemberManagement.this, "Enter Member Password:");

            if (email != null && password != null) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {

                    stmt.setString(1, email);
                    stmt.setString(2, password);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(MemberManagement.this, "Member added successfully!");
                    loadMembers();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Error adding member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class EditMemberAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Please select a member to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int memberId = (int) memberTable.getValueAt(selectedRow, 0);
            String newEmail = JOptionPane.showInputDialog(MemberManagement.this, "Enter New Email:", memberTable.getValueAt(selectedRow, 1));
            
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(MemberManagement.this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;}
            
            String newPassword = JOptionPane.showInputDialog(MemberManagement.this, "Enter New Password:");

             if (newPassword.length() < 8) {
            JOptionPane.showMessageDialog(MemberManagement.this, "Password must be at least 8 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
              }
                    
           

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE users SET email = ?, password = ? WHERE id = ?")) {

                    stmt.setString(1, newEmail);
                    stmt.setString(2, newPassword);
                    stmt.setInt(3, memberId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(MemberManagement.this, "Member updated successfully!");
                    loadMembers();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Error updating member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    

    private class DeleteMemberAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Please select a member to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int memberId = (int) memberTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(MemberManagement.this, "Are you sure you want to delete this member?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(MemberManagement.this, "Member deleted successfully!");
                    loadMembers();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Error deleting member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

        }
