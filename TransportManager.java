import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class TransportManager extends JFrame {

    Connection conn;
    JTextField[] driverFields = new JTextField[4];
    JTextField[] routeFields = new JTextField[5];
    JTextField[] busFields = new JTextField[6]; // Bus basic fields (excluding dropdowns)
    JComboBox<String> routeBox, driverBox;

    JTable driverTable, routeTable, busTable;

    public TransportManager() {
        setTitle("🚍 Transport Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectDatabase();

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("🧑 Drivers", createDriverPanel());
        tabs.addTab("🛣️ Routes", createRoutePanel());
        tabs.addTab("🚌 Buses", createBusPanel());
        tabs.addTab("📋 Reports", createReportPanel());

        add(tabs, BorderLayout.CENTER);

        JButton shortestBtn = new JButton("🔍 Find Shortest Route");
        shortestBtn.addActionListener(e -> {
            String result = getShortestRoute();
            JOptionPane.showMessageDialog(this, result, "Shortest Route", JOptionPane.INFORMATION_MESSAGE);
        });
        add(shortestBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/transportdb?useSSL=false&serverTimezone=UTC",
                "root",
                "srikar2811"
            );
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ DB Connection Failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private JPanel createDriverPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        String[] labels = {"Name", "License Number", "Contact Number", "Status"};

        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            driverFields[i] = new JTextField();
            form.add(driverFields[i]);
        }

        JButton addBtn = new JButton("➕ Add Driver");
        addBtn.addActionListener(e -> insertDriver());

        driverTable = new JTable();
        JButton viewBtn = new JButton("📄 View Drivers");
        viewBtn.addActionListener(e -> loadTable("Driver", driverTable));

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(viewBtn);

        panel.add(form, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(driverTable), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRoutePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        String[] labels = {"Start Location", "End Location", "Distance (km)", "Estimated Time (hrs)", "Stops"};

        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            routeFields[i] = new JTextField();
            form.add(routeFields[i]);
        }

        JButton addBtn = new JButton("➕ Add Route");
        addBtn.addActionListener(e -> insertRoute());

        routeTable = new JTable();
        JButton viewBtn = new JButton("📄 View Routes");
        viewBtn.addActionListener(e -> loadTable("Route", routeTable));

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(viewBtn);

        panel.add(form, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(routeTable), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        String[] labels = {
            "Registration Number", "Capacity", "Model",
            "Status", "Departure Time", "Arrival Time"
        };

        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            busFields[i] = new JTextField();
            form.add(busFields[i]);
        }

        routeBox = new JComboBox<>();
        driverBox = new JComboBox<>();

        populateComboBox(routeBox, "SELECT RouteID FROM Route");
        populateComboBox(driverBox, "SELECT DriverID FROM Driver");

        form.add(new JLabel("Route ID"));
        form.add(routeBox);
        form.add(new JLabel("Driver ID"));
        form.add(driverBox);

        JButton addBtn = new JButton("➕ Add Bus");
        addBtn.addActionListener(e -> insertBus());

        busTable = new JTable();
        JButton viewBtn = new JButton("📄 View Buses");
        viewBtn.addActionListener(e -> loadTable("Bus", busTable));

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(viewBtn);

        panel.add(form, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(busTable), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        JTextArea report = new JTextArea(15, 60);
        report.setEditable(false);

        JButton refresh = new JButton("📊 Generate Summary Report");
        refresh.addActionListener(e -> {
            report.setText(generateSummaryReport());
        });

        panel.add(new JScrollPane(report));
        panel.add(refresh);

        return panel;
    }

    private void insertDriver() {
        String query = "INSERT INTO Driver(Name, LicenseNumber, ContactNumber, Status) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            for (int i = 0; i < driverFields.length; i++) {
                pst.setString(i + 1, driverFields[i].getText().trim());
            }
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Driver added!");
            for (JTextField f : driverFields) f.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    private void insertRoute() {
        String query = "INSERT INTO Route(StartLocation, EndLocation, Distance, EstimatedTime, Stops) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, routeFields[0].getText().trim());
            pst.setString(2, routeFields[1].getText().trim());
            pst.setDouble(3, Double.parseDouble(routeFields[2].getText().trim()));
            pst.setDouble(4, Double.parseDouble(routeFields[3].getText().trim()));
            pst.setString(5, routeFields[4].getText().trim());

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Route added!");
            for (JTextField f : routeFields) f.setText("");

            populateComboBox(routeBox, "SELECT RouteID FROM Route"); // refresh RouteID options
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    private void insertBus() {
        String query = "INSERT INTO Bus(RegistrationNumber, Capacity, Model, Status, DepartureTime, ArrivalTime, RouteID, DriverID) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            for (int i = 0; i < 6; i++) {
                pst.setString(i + 1, busFields[i].getText().trim());
            }

            pst.setInt(7, Integer.parseInt((String) routeBox.getSelectedItem()));
            pst.setInt(8, Integer.parseInt((String) driverBox.getSelectedItem()));

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Bus added!");
            for (JTextField f : busFields) f.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    private void populateComboBox(JComboBox<String> comboBox, String query) {
        comboBox.removeAllItems();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                comboBox.addItem(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTable(String tableName, JTable table) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)) {
            ResultSetMetaData meta = rs.getMetaData();
            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                colNames.add(meta.getColumnName(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, colNames);
            table.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getShortestRoute() {
        StringBuilder sb = new StringBuilder();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Route ORDER BY Distance ASC LIMIT 1")) {
            if (rs.next()) {
                sb.append("📍 Shortest Route:\n")
                        .append("From: ").append(rs.getString("StartLocation")).append("\n")
                        .append("To: ").append(rs.getString("EndLocation")).append("\n")
                        .append("Distance: ").append(rs.getDouble("Distance")).append(" km\n")
                        .append("Estimated Time: ").append(rs.getDouble("EstimatedTime")).append(" hrs");
            } else {
                sb.append("No route data available.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error: ").append(e.getMessage());
        }
        return sb.toString();
    }

    private String generateSummaryReport() {
        StringBuilder report = new StringBuilder();
        try (Statement st = conn.createStatement()) {
            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM Driver");
            rs1.next();
            report.append("🧑 Total Drivers: ").append(rs1.getInt(1)).append("\n");

            rs1 = st.executeQuery("SELECT COUNT(*) FROM Route");
            rs1.next();
            report.append("🛣️ Total Routes: ").append(rs1.getInt(1)).append("\n");

            rs1 = st.executeQuery("SELECT COUNT(*) FROM Bus");
            rs1.next();
            

        } catch (Exception e) {
            e.printStackTrace();
            report.append("❌ Error generating report: ").append(e.getMessage());
        }

        return report.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TransportManager::new);
    }
}
