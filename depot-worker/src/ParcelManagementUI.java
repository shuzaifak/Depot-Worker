import java.awt.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

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
    private JTextField searchField;
    private JComboBox<String> searchCriteriaBox;
    private JComboBox<String> sortComboBox;

    // Additional UI components
    private JButton generateReportBtn;
    private JButton addNewParcelBtn;
    private JButton findParcelBtn;
    private JTextField reportFileField;

    // Styling constants
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);
    private static final Color ACCENT_COLOR = new Color(51, 102, 204);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public ParcelManagementUI() {
        manager = new Manager();
        setupUI();
        customizeAppearance();
    }

    private void customizeAppearance() {
        UIManager.put("Button.background", PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", REGULAR_FONT);
        UIManager.put("Label.font", REGULAR_FONT);
        UIManager.put("TextField.font", REGULAR_FONT);
        UIManager.put("TextArea.font", REGULAR_FONT);
        UIManager.put("Table.font", REGULAR_FONT);
        UIManager.put("TableHeader.font", HEADER_FONT);
    }

    private void setupUI() {
        setTitle("Parcel Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(SECONDARY_COLOR);

        // Main panels
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBackground(SECONDARY_COLOR);
        
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(SECONDARY_COLOR);
        
        // Add control sections
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // File operations
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(createFileLoadingPanel(), gbc);

        // Customer management
        gbc.gridy = 1;
        controlPanel.add(createCustomerManagementPanel(), gbc);

        // Operations panel
        gbc.gridy = 2;
        JPanel operationsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        operationsPanel.setBackground(SECONDARY_COLOR);
        operationsPanel.add(createParcelAssignmentPanel());
        operationsPanel.add(createProcessAndLogPanel());
        controlPanel.add(operationsPanel, gbc);

        topPanel.add(controlPanel);

        // Additional controls
        JPanel additionalControlsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        additionalControlsPanel.setBackground(SECONDARY_COLOR);
        addAdditionalControls(additionalControlsPanel);
        topPanel.add(additionalControlsPanel);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(SECONDARY_COLOR);
        contentPanel.add(createSearchAndTablePanel(), BorderLayout.CENTER);

        // Log area
        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        logArea.setFont(REGULAR_FONT);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(createStyledTitledBorder("System Log"));
        contentPanel.add(logScroll, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setSize(1200, 900);
    }

    private Border createStyledTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            HEADER_FONT,
            PRIMARY_COLOR
        );
    }

    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SECONDARY_COLOR);
        panel.setBorder(createStyledTitledBorder(title));
        return panel;
    }

    private JPanel createFileLoadingPanel() {
        JPanel panel = createStyledPanel("File Loading");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        customerFileField = new JTextField(20);
        parcelFileField = new JTextField(20);
        JButton loadCustomerButton = new JButton("Load Customer Data");
        JButton loadParcelButton = new JButton("Load Parcel Data");

        loadCustomerButton.addActionListener(e -> loadCustomerData());
        loadParcelButton.addActionListener(e -> loadParcelData());

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

    private JPanel createCustomerManagementPanel() {
        JPanel panel = createStyledPanel("Customer Management");
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

    private JPanel createParcelAssignmentPanel() {
        JPanel panel = createStyledPanel("Parcel Assignment");
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
        JPanel panel = createStyledPanel("Processing");
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

    private void addAdditionalControls(JPanel container) {
        // Report Generation Panel
        JPanel reportPanel = createStyledPanel("Report Generation");
        reportPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        reportFileField = new JTextField(20);
        generateReportBtn = new JButton("Generate Report");
        generateReportBtn.addActionListener(e -> generateReport());
        
        reportPanel.add(new JLabel("Report Filename:"));
        reportPanel.add(reportFileField);
        reportPanel.add(generateReportBtn);

        // New Parcel Panel
        JPanel newParcelPanel = createStyledPanel("Add New Parcel");
        newParcelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JTextField newParcelIdField = new JTextField(10);
        JTextField newParcelWeightField = new JTextField(10);
        JComboBox<String> newParcelTypeBox = new JComboBox<>(
            new String[]{"Standard", "Fragile", "Perishable"}
        );
        addNewParcelBtn = new JButton("Add New Parcel");
        
        addNewParcelBtn.addActionListener(e -> {
            try {
                double weight = Double.parseDouble(newParcelWeightField.getText());
                manager.addNewParcel(
                    newParcelIdField.getText(),
                    weight,
                    (String)newParcelTypeBox.getSelectedItem()
                );
                refreshParcelTable();
                appendToLog("Added new parcel: " + newParcelIdField.getText());
            } catch (NumberFormatException ex) {
                showError("Invalid weight format");
            }
        });

        newParcelPanel.add(new JLabel("New Parcel ID:"));
        newParcelPanel.add(newParcelIdField);
        newParcelPanel.add(new JLabel("Weight:"));
        newParcelPanel.add(newParcelWeightField);
        newParcelPanel.add(new JLabel("Type:"));
        newParcelPanel.add(newParcelTypeBox);
        newParcelPanel.add(addNewParcelBtn);

        // Find Parcel Panel
        JPanel findParcelPanel = createStyledPanel("Find Parcel");
        findParcelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JTextField findParcelIdField = new JTextField(10);
        findParcelBtn = new JButton("Find Parcel");
        findParcelBtn.addActionListener(e -> {
            String id = findParcelIdField.getText();
            Parcel found = manager.findParcelById(id);
            if (found != null) {
                highlightParcelInTable(found);
                appendToLog("Found parcel: " + id);
            } else {
                showError("Parcel not found: " + id);
            }
        });

        findParcelPanel.add(new JLabel("Find Parcel ID:"));
        findParcelPanel.add(findParcelIdField);
        findParcelPanel.add(findParcelBtn);

        container.add(reportPanel);
        container.add(newParcelPanel);
        container.add(findParcelPanel);
    }

    private JPanel createSearchAndTablePanel() {
        JPanel panel = createStyledPanel("Parcel Management");
        panel.setLayout(new BorderLayout(5, 5));

        // Search controls
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(SECONDARY_COLOR);
        
        searchField = new JTextField(20);
        searchCriteriaBox = new JComboBox<>(new String[]{
            "ID", "Type", "Weight Range", "Status"
        });
        sortComboBox = new JComboBox<>(new String[]{
            "ID", "Weight", "Type", "Fee"
        });
        
        JButton sortButton = new JButton("Sort");
        sortButton.addActionListener(e -> sortParcels());
        
        JLabel searchLabel = new JLabel("Search Parcels:");
        searchLabel.setFont(HEADER_FONT);
        searchLabel.setForeground(PRIMARY_COLOR);
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("by"));
        searchPanel.add(searchCriteriaBox);
        searchPanel.add(new JLabel("Sort by:"));
        searchPanel.add(sortComboBox);
        searchPanel.add(sortButton);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { searchParcels(); }
           public void removeUpdate(DocumentEvent e) { searchParcels(); }
            public void insertUpdate(DocumentEvent e) { searchParcels(); }
        });

        // Setup table
        setupParcelTable();
        JScrollPane tableScrollPane = new JScrollPane(parcelTable);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupParcelTable() {
        String[] columns = {"ID", "Weight", "Type", "Status", "Fee"};
        parcelTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        parcelTable = new JTable(parcelTableModel);
        parcelTable.setFont(REGULAR_FONT);
        parcelTable.getTableHeader().setFont(HEADER_FONT);
        parcelTable.setRowHeight(25);
        parcelTable.setShowGrid(true);
        parcelTable.setGridColor(SECONDARY_COLOR);
        parcelTable.setSelectionBackground(PRIMARY_COLOR);
        parcelTable.setSelectionForeground(Color.WHITE);
    }

    private void searchParcels() {
        String searchTerm = searchField.getText().toLowerCase();
        String criteria = (String) searchCriteriaBox.getSelectedItem();
        Collection<Parcel> allParcels = manager.getAllParcels();
        Vector<Vector<Object>> filteredData = new Vector<>();

        for (Parcel parcel : allParcels) {
            boolean matches = switch (criteria) {
                case "ID" -> parcel.getId().toLowerCase().contains(searchTerm);
                case "Type" -> parcel.getType().toLowerCase().contains(searchTerm);
                case "Weight Range" -> {
                    try {
                        double weight = Double.parseDouble(searchTerm);
                        yield Math.abs(parcel.getWeight() - weight) <= 5;
                    } catch (NumberFormatException e) {
                        yield false;
                    }
                }
                case "Status" -> String.valueOf(parcel.isProcessed()).toLowerCase().contains(searchTerm);
                default -> false;
            };

            if (matches || searchTerm.isEmpty()) {
                Vector<Object> row = new Vector<>();
                row.add(parcel.getId());
                row.add(String.format("%.2f", parcel.getWeight()));
                row.add(parcel.getType());
                row.add(parcel.isProcessed() ? "Processed" : "Pending");
                row.add(String.format("$%.2f", parcel.getFee()));
                filteredData.add(row);
            }
        }

        parcelTableModel.setDataVector(filteredData, new Vector<>(Arrays.asList(
            "ID", "Weight", "Type", "Status", "Fee")));
    }

    private void sortParcels() {
        String sortBy = (String) sortComboBox.getSelectedItem();
        java.util.List<Parcel> parcels = new ArrayList<>(manager.getAllParcels());
        
        switch (sortBy) {
            case "ID" -> parcels.sort(Comparator.comparing(Parcel::getId));
            case "Weight" -> parcels.sort(Comparator.comparing(Parcel::getWeight));
            case "Type" -> parcels.sort(Comparator.comparing(Parcel::getType));
            case "Fee" -> parcels.sort(Comparator.comparing(Parcel::getFee));
        }

        refreshParcelTable(parcels);
        appendToLog("Sorted parcels by: " + sortBy);
    }

    private void refreshParcelTable() {
        refreshParcelTable(manager.getAllParcels());
    }

    private void refreshParcelTable(Collection<Parcel> parcels) {
        parcelTableModel.setRowCount(0);
        for (Parcel parcel : parcels) {
            parcelTableModel.addRow(new Object[]{
                parcel.getId(),
                String.format("%.2f", parcel.getWeight()),
                parcel.getType(),
                parcel.isProcessed() ? "Processed" : "Pending",
                String.format("$%.2f", parcel.getFee())
            });
        }
    }

    private void highlightParcelInTable(Parcel parcel) {
        for (int i = 0; i < parcelTableModel.getRowCount(); i++) {
            if (parcelTableModel.getValueAt(i, 0).equals(parcel.getId())) {
                parcelTable.setRowSelectionInterval(i, i);
                parcelTable.scrollRectToVisible(parcelTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private void loadCustomerData() {
        String filename = customerFileField.getText().trim();
        if (!filename.isEmpty()) {
            try {
                manager.loadCustomerData(filename);
                appendToLog("Loaded customer data from: " + filename);
            } catch (Exception e) {
                showError("Error loading customer data: " + e.getMessage());
            }
        } else {
            showError("Please enter a customer data filename");
        }
    }

    private void loadParcelData() {
        String filename = parcelFileField.getText().trim();
        if (!filename.isEmpty()) {
            try {
                manager.loadParcelData(filename);
                appendToLog("Loaded parcel data from: " + filename);
                refreshParcelTable();
            } catch (Exception e) {
                showError("Error loading parcel data: " + e.getMessage());
            }
        } else {
            showError("Please enter a parcel data filename");
        }
    }

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

    private void assignParcel() {
        String customerId = customerIdField.getText().trim();
        String parcelId = parcelIdField.getText().trim();
        if (!customerId.isEmpty() && !parcelId.isEmpty()) {
            try {
                manager.assignParcelToCustomer(customerId, parcelId);
                appendToLog("Assigned parcel " + parcelId + " to customer " + customerId);
                customerIdField.setText("");
                parcelIdField.setText("");
                refreshParcelTable();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        } else {
            showError("Please enter both Customer ID and Parcel ID");
        }
    }

    private void processNextCustomer() {
        try {
            Customer processed = manager.processNextCustomer();
            if (processed != null) {
                appendToLog("Processed customer: " + processed.getName() + " (ID: " + processed.getId() + ")");
                refreshParcelTable();
            } else {
                appendToLog("No customers in queue to process");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void saveLog() {
        String filename = logFileField.getText().trim();
        if (!filename.isEmpty()) {
            try {
                File file = new File("data/" + filename);
                Log.getInstance().saveToFile(file.getPath());
                appendToLog("Log saved to: " + filename);
            } catch (Exception e) {
                showError("Error saving log: " + e.getMessage());
            }
        } else {
            showError("Please enter a log filename");
        }
    }

    private void generateReport() {
        String filename = reportFileField.getText().trim();
        if (!filename.isEmpty()) {
            try {
                manager.processAndGenerateReport(filename);
                appendToLog("Generated report: " + filename);
            } catch (Exception e) {
                showError("Error generating report: " + e.getMessage());
            }
        } else {
            showError("Please enter a report filename");
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
                new ParcelManagementUI().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}