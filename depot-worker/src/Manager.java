import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Scanner;

class Manager {
    private QueueOfCustomers customerQueue;
    private ParcelMap parcelMap;
    private Worker worker;
    private Log log;

    public Manager() {
        customerQueue = new QueueOfCustomers();
        parcelMap = new ParcelMap();
        worker = new Worker();
        log = Log.getInstance();
    }
 public void addCustomerToQueue(Customer customer) throws ValidationException {
        customerQueue.addCustomer(customer);
        log.addEntry("Added new customer: " + customer.getName());
    }

    public Customer removeNextCustomer() {
        Customer customer = customerQueue.removeCustomer();
        if (customer != null) {
            log.addEntry("Removed customer: " + customer.getName());
        }
        return customer;
    }

    public Collection<Parcel> getAllParcels() {
        return parcelMap.getAllParcels();
    }
    public void loadCustomerData(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("Error: Filename cannot be empty");
            return;
        }

        File file = new File("data/" + filename);
        if (!file.exists()) {
            System.err.println("Error: File not found in data directory");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    processCustomerLine(line);
                } catch (ValidationException e) {
                    System.err.println("Error on line " + lineNumber + ": " + e.getMessage());
                }
            }
            System.out.println("Successfully loaded customer data.");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void processCustomerLine(String line) throws ValidationException {
        if (line.trim().isEmpty()) {
            throw new ValidationException("Empty line found");
        }

        String[] parts = line.split(",");
        if (parts.length != 2) {
            throw new ValidationException("Invalid format. Expected: ID,Name");
        }

        Customer customer = new Customer(parts[0].trim(), parts[1].trim());
        customerQueue.addCustomer(customer);
        log.addEntry("Loaded customer: " + customer.getName());
    }

    public void loadParcelData(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("Error: Filename cannot be empty");
            return;
        }

        File file = new File("data/" + filename);
        if (!file.exists()) {
            System.err.println("Error: File not found in data directory");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    processParcelLine(line);
                } catch (ValidationException | NumberFormatException e) {
                    System.err.println("Error on line " + lineNumber + ": " + e.getMessage());
                }
            }
            System.out.println("Successfully loaded parcel data.");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void processParcelLine(String line) throws ValidationException {
        if (line.trim().isEmpty()) {
            throw new ValidationException("Empty line found");
        }

        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new ValidationException("Invalid format. Expected: ID,Weight,Type");
        }

        try {
            Parcel parcel = new Parcel(
                parts[0].trim(),
                Double.parseDouble(parts[1].trim()),
                parts[2].trim()
            );
            parcelMap.addParcel(parcel);
            log.addEntry("Loaded parcel: " + parcel.getId());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid weight format: " + parts[1]);
        }
    }

    public Customer processNextCustomer() {
        // Check if there are any customers in the queue
        if (customerQueue.isEmpty()) {
            return null;
        }
    
        // Get the next customer from the queue
        Customer customer = customerQueue.peekCustomer();
        
        // If customer was null or has no parcels, just remove them and return
        if (customer == null || customer.getParcels().isEmpty()) {
            customerQueue.removeCustomer();
            return customer;
        }
    
        // Process all parcels for this customer
        for (Parcel parcel : customer.getParcels()) {
            // Calculate fee based on parcel type and weight
            double fee = calculateParcelFee(parcel);
            parcel.setFee(fee);
            parcel.setProcessed(true);
            
            // Add processing details to log
            Log.getInstance().addEntry(String.format(
                "Processed parcel %s for customer %s (%s) - Fee: $%.2f",
                parcel.getId(),
                customer.getId(),
                customer.getName(),
                fee
            ));
        }
        
        // Remove the customer after processing
        customerQueue.removeCustomer();
        
        // Return the processed customer
        return customer;
    }
    
    private double calculateParcelFee(Parcel parcel) {
        double baseFee = parcel.getWeight() * 2.5; // Base fee of $2.50 per kg
        
        // Add additional fee based on parcel type
        return switch (parcel.getType().toLowerCase()) {
            case "fragile" -> baseFee * 1.5;    // 50% extra for fragile items
            case "perishable" -> baseFee * 1.3;  // 30% extra for perishable items
            default -> baseFee;                  // Standard fee for regular parcels
        };
    }
    
    private Customer findCustomer(String customerId) throws ValidationException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be empty");
        }
    
        if (!customerId.matches("C\\d{3}")) {
            throw new ValidationException("Invalid customer ID format. Must be C followed by 3 digits");
        }
    
        // Create a temporary queue to hold customers
        QueueOfCustomers tempQueue = new QueueOfCustomers();
        Customer foundCustomer = null;
    
        // Iterate through the original queue
        while (!customerQueue.isEmpty()) {
            Customer current = customerQueue.removeCustomer();
    
            // Check if this is the customer we are looking for
            if (current.getId().equals(customerId)) {
                foundCustomer = current;
            }
    
            // Add the customer back to the temporary queue
            tempQueue.addCustomer(current);
        }
    
        // Restore the original queue
        while (!tempQueue.isEmpty()) {
            customerQueue.addCustomer(tempQueue.removeCustomer());
        }
    
        return foundCustomer; // Return null if not found
    }
    
      public void processAndGenerateReport(String reportFilename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/" + reportFilename))) {
            writer.println("Parcel Processing Report");
            writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.println("----------------------------------------");

            while (!customerQueue.isEmpty()) {
                Customer customer = customerQueue.removeCustomer();
                writer.printf("\nProcessing customer: %s (%s)%n", customer.getName(), customer.getId());

                for (Parcel parcel : customer.getParcels()) {
                    try {
                        worker.processParcel(parcel);
                        parcel.setStatus(ParcelStatus.COLLECTED);
                        writer.printf("Processed parcel %s: Fee=%.2f, Status=%s%n",
                            parcel.getId(), parcel.getFee(), parcel.getStatus());
                    } catch (ValidationException e) {
                        writer.printf("Error processing parcel %s: %s%n", parcel.getId(), e.getMessage());
                    }
                }
            }
            writer.println("\nEnd of Report");
        } catch (IOException e) {
            log.addEntry("Error generating report: " + e.getMessage());
        }
    }

    public void addNewParcel(String id, double weight, String type) {
        try {
            Parcel newParcel = new Parcel(id, weight, type);
            parcelMap.addParcel(newParcel);
            log.addEntry("Added new parcel: " + id);
        } catch (ValidationException e) {
            log.addEntry("Error adding parcel: " + e.getMessage());
        }
    }

    public Parcel findParcelById(String id) {
        return parcelMap.findParcel(id);
    }

    public void assignParcelToCustomer(String customerId, String parcelId) {
        try {
            validateIds(customerId, parcelId);
            Customer customer = findCustomer(customerId);
            Parcel parcel = parcelMap.findParcel(parcelId);

            if (customer == null) {
                throw new ValidationException("Customer not found: " + customerId);
            }
            if (parcel == null) {
                throw new ValidationException("Parcel not found: " + parcelId);
            }
            if (parcel.isProcessed()) {
                throw new ValidationException("Parcel has already been processed");
            }

            customer.addParcel(parcel);
            System.out.println("Successfully assigned parcel " + parcelId + 
                             " to customer " + customer.getName());
            log.addEntry("Assigned parcel " + parcelId + " to customer " + customer.getName());
        } catch (ValidationException e) {
            System.err.println("Error assigning parcel: " + e.getMessage());
        }
    }

    private void validateIds(String customerId, String parcelId) throws ValidationException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be empty");
        }
        if (parcelId == null || parcelId.trim().isEmpty()) {
            throw new ValidationException("Parcel ID cannot be empty");
        }
        if (!customerId.matches("C\\d{3}")) {
            throw new ValidationException("Invalid customer ID format. Must be C followed by 3 digits");
        }
        if (!parcelId.matches("P\\d{3}")) {
            throw new ValidationException("Invalid parcel ID format. Must be P followed by 3 digits");
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            try {
                System.out.println("\n1. Load Customer Data");
                System.out.println("2. Load Parcel Data");
                System.out.println("3. Assign Parcel to Customer");
                System.out.println("4. Process Next Customer");
                System.out.println("5. Save Log");
                System.out.println("6. Exit");
                System.out.print("Enter choice (1-6): ");

                String input = scanner.nextLine().trim();
                if (!input.matches("[1-6]")) {
                    System.err.println("Invalid choice. Please enter a number between 1 and 6.");
                    continue;
                }

                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter customer data filename: ");
                        String filename = scanner.nextLine().trim();
                        loadCustomerData(filename);
                    }
                    case 2 -> {
                        System.out.print("Enter parcel data filename: ");
                        String filename = scanner.nextLine().trim();
                        loadParcelData(filename);
                    }
                    case 3 -> {
                        System.out.print("Enter customer ID (Cxxx): ");
                        String customerId = scanner.nextLine().trim();
                        System.out.print("Enter parcel ID (Pxxx): ");
                        String parcelId = scanner.nextLine().trim();
                        assignParcelToCustomer(customerId, parcelId);
                    }
                    case 4 -> processNextCustomer();
                    case 5 -> {
                        System.out.print("Enter log filename: ");
                        String filename = scanner.nextLine().trim();
                        if (filename.isEmpty()) {
                            System.err.println("Filename cannot be empty");
                        } else {
                            log.saveToFile("data/" + filename);
                            System.out.println("Log saved successfully.");
                        }
                    }
                    case 6 -> {
                        running = false;
                        System.out.println("Exiting program...");
                    }
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                log.addEntry("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }



    public static void main(String[] args) {
        Manager manager = new Manager();
        manager.run();
    }
}