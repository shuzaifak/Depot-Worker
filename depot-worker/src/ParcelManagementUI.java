import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class ParcelManagementUI extends JFrame {
    private Manager manager;
    private JTextArea logArea;
    private JTextField customerFileField;
    private JTextField parcelFileField;
    private JTextField customerIdField;
    private JTextField parcelIdField;
    private JTextField logFileField;
    private JTextField newCustomerIdField;
    private JTextField newCustomerNameField;
    private JTable parcelTable;
    private DefaultTableModel parcelTableModel;
    private JComboBox<String> sortComboBox;

    public ParcelManagementUI() {
        manager = new Manager();
        setupUI();
    }

    private void setupUI() {
        setTitle("Parcel Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create main panels
        JPanel controlPanel = new JPanel(new GridBagLayout());
        JPanel logPanel = new JPanel(new BorderLayout());

        // Setup control panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add all panels
        JPanel filePanel = createFileLoadingPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(filePanel, gbc);

        JPanel customerPanel = createCustomerManagementPanel();
        gbc.gridy = 1;
        controlPanel.add(customerPanel, gbc);

        JPanel assignmentPanel = createParcelAssignmentPanel();
        gbc.gridy = 2;
        controlPanel.add(assignmentPanel, gbc);

        JPanel processPanel = createProcessAndLogPanel();
        gbc.gridy = 3;
        controlPanel.add(processPanel, gbc);

        // Create parcel table panel
        JPanel parcelTablePanel = createParcelTablePanel();
        
        // Create split pane for log and parcel table
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Log area
        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        JPanel wrappedLogPanel = new JPanel(new BorderLayout());
        wrappedLogPanel.add(new JLabel("System Log"), BorderLayout.NORTH);
        wrappedLogPanel.add(logScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(parcelTablePanel);
        splitPane.setBottomComponent(wrappedLogPanel);
        splitPane.setResizeWeight(0.5);

        // Add panels to frame
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setSize(800, 800);
    }

    private JPanel createCustomerManagementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        newCustomerIdField = new JTextField(10);
        newCustomerNameField = new JTextField(20);
        JButton addCustomerButton = new JButton("Add New Customer");
        JButton removeCustomerButton = new JButton("Remove Next Customer");

        addCustomerButton.addActionListener(e -> addNewCustomer());
        removeCustomerButton.addActionListener(e -> removeNextCustomer());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Customer ID (Cxxx):"), gbc);
        gbc.gridx = 1;
        panel.add(newCustomerIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        panel.add(newCustomerNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addCustomerButton, gbc);

        gbc.gridy = 3;
        panel.add(removeCustomerButton, gbc);

        return panel;
    }

    private JPanel createParcelTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Parcel List"));

        // Create table model with columns
        String[] columns = {"ID", "Weight", "Type", "Processed", "Fee"};
        parcelTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        parcelTable = new JTable(parcelTableModel);

        // Create sorting controls
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortComboBox = new JComboBox<>(new String[]{"ID", "Weight", "Type", "Fee"});
        JButton sortButton = new JButton("Sort");
        sortButton.addActionListener(e -> sortParcels());

        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(sortComboBox);
        sortPanel.add(sortButton);

        // Add components to panel
        panel.add(sortPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(parcelTable), BorderLayout.CENTER);

        return panel;
    }

    // Previous panels remain the same...

    private void addNewCustomer() {
        String id = newCustomerIdField.getText().trim();
        String name = newCustomerNameField.getText().trim();
        
        try {
            Customer newCustomer = new Customer(id, name);
            manager.addCustomerToQueue(newCustomer);
            appendToLog("Added new customer: " + name + " (ID: " + id + ")");
            newCustomerIdField.setText("");
            newCustomerNameField.setText("");
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    private void removeNextCustomer() {
        try {
            Customer removed = manager.removeNextCustomer();
            if (removed != null) {
                appendToLog("Removed customer: " + removed.getName() + " (ID: " + removed.getId() + ")");
            } else {
                appendToLog("No customers in queue to remove");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void sortParcels() {
        String sortBy = (String) sortComboBox.getSelectedItem();
        List<Parcel> parcels = new ArrayList<>(manager.getAllParcels());
        
        switch (sortBy) {
            case "ID" -> parcels.sort(Comparator.comparing(Parcel::getId));
            case "Weight" -> parcels.sort(Comparator.comparing(Parcel::getWeight));
            case "Type" -> parcels.sort(Comparator.comparing(Parcel::getType));
            case "Fee" -> parcels.sort(Comparator.comparing(Parcel::getFee));
        }

        updateParcelTable(parcels);
        appendToLog("Sorted parcels by: " + sortBy);
    }

    private void updateParcelTable(List<Parcel> parcels) {
        parcelTableModel.setRowCount(0);
        for (Parcel parcel : parcels) {
            parcelTableModel.addRow(new Object[]{
                parcel.getId(),
                parcel.getWeight(),
                parcel.getType(),
                parcel.isProcessed(),
                String.format("%.2f", parcel.getFee())
            });
        }
    }

    private JPanel createFileLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("File Loading"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Customer file loading
        customerFileField = new JTextField(20);
        JButton loadCustomerButton = new JButton("Load Customer Data");
        loadCustomerButton.addActionListener(e -> loadCustomerData());

        // Parcel file loading
        parcelFileField = new JTextField(20);
        JButton loadParcelButton = new JButton("Load Parcel Data");
        loadParcelButton.addActionListener(e -> loadParcelData());

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Customer File:"), gbc);
        gbc.gridx = 1;
        panel.add(customerFileField, gbc);
        gbc.gridx = 2;
        panel.add(loadCustomerButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Parcel File:"), gbc);
        gbc.gridx = 1;
        panel.add(parcelFileField, gbc);
        gbc.gridx = 2;
        panel.add(loadParcelButton, gbc);

        return panel;
    }

    private JPanel createParcelAssignmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Parcel Assignment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        customerIdField = new JTextField(10);
        parcelIdField = new JTextField(10);
        JButton assignButton = new JButton("Assign Parcel");
        assignButton.addActionListener(e -> assignParcel());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Customer ID (Cxxx):"), gbc);
        gbc.gridx = 1;
        panel.add(customerIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Parcel ID (Pxxx):"), gbc);
        gbc.gridx = 1;
        panel.add(parcelIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(assignButton, gbc);

        return panel;
    }

    private JPanel createProcessAndLogPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Processing"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        logFileField = new JTextField(20);
        JButton processButton = new JButton("Process Next Customer");
        JButton saveLogButton = new JButton("Save Log");

        processButton.addActionListener(e -> processNextCustomer());
        saveLogButton.addActionListener(e -> saveLog());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(processButton, gbc);

        gbc.gridy = 1;
        panel.add(new JLabel("Log File:"), gbc);
        gbc.gridx = 1;
        panel.add(logFileField, gbc);
        gbc.gridx = 2;
        panel.add(saveLogButton, gbc);

        return panel;
    }

    private void loadCustomerData() {
        String filename = customerFileField.getText().trim();
        if (!filename.isEmpty()) {
            manager.loadCustomerData(filename);
            appendToLog("Attempted to load customer data from: " + filename);
        } else {
            showError("Please enter a customer data filename");
        }
    }

    private void loadParcelData() {
        String filename = parcelFileField.getText().trim();
        if (!filename.isEmpty()) {
            manager.loadParcelData(filename);
            appendToLog("Attempted to load parcel data from: " + filename);
        } else {
            showError("Please enter a parcel data filename");
        }
    }

    private void assignParcel() {
        String customerId = customerIdField.getText().trim();
        String parcelId = parcelIdField.getText().trim();
        if (!customerId.isEmpty() && !parcelId.isEmpty()) {
            manager.assignParcelToCustomer(customerId, parcelId);
            customerIdField.setText("");
            parcelIdField.setText("");
        } else {
            showError("Please enter both Customer ID and Parcel ID");
        }
    }

    private void processNextCustomer() {
        manager.processNextCustomer();
    }

    private void saveLog() {
        String filename = logFileField.getText().trim();
        if (!filename.isEmpty()) {
            File file = new File("data/" + filename);
            Log.getInstance().saveToFile(file.getPath());
            appendToLog("Log saved to: " + filename);
        } else {
            showError("Please enter a log filename");
        }
    }

    private void appendToLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ParcelManagementUI().setVisible(true);
        });
    }
}