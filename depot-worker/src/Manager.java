import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
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

    public void processNextCustomer() {
        Customer customer = customerQueue.removeCustomer();
        if (customer == null) {
            System.out.println("No customers in queue.");
            return;
        }

        System.out.println("Processing customer: " + customer.getName());
        log.addEntry("Processing customer: " + customer.getName());

        if (customer.getParcels().isEmpty()) {
            System.out.println("Customer has no parcels to process.");
            return;
        }

        for (Parcel parcel : customer.getParcels()) {
            try {
                worker.processParcel(parcel);
                System.out.println("Processed parcel " + parcel.getId() + 
                                 " with fee: $" + String.format("%.2f", parcel.getFee()));
            } catch (ValidationException e) {
                System.err.println("Error processing parcel " + parcel.getId() + ": " + e.getMessage());
            }
        }
    }
    private Customer findCustomer(String customerId) throws ValidationException {
    if (customerId == null || customerId.trim().isEmpty()) {
        throw new ValidationException("Customer ID cannot be empty");
    }
    
    if (!customerId.matches("C\\d{3}")) {
        throw new ValidationException("Invalid customer ID format. Must be C followed by 3 digits");
    }
    
    // Iterate through the queue to find the customer
    // We need to create a temporary queue to maintain the original order
    Queue<Customer> tempQueue = new LinkedList<>();
    Customer foundCustomer = null;
    
    while (!customerQueue.isEmpty()) {
        Customer current = customerQueue.removeCustomer();
        if (current.getId().equals(customerId)) {
            foundCustomer = current;
        }
        ((QueueOfCustomers) tempQueue).addCustomer(current);
    }
    
    // Restore the original queue
    while (!tempQueue.isEmpty()) {
        customerQueue.addCustomer(((QueueOfCustomers) tempQueue).removeCustomer());
    }
    
    return foundCustomer;
}
    public void assignParcelToCustomer(String customerId, String parcelId) {
        try {
            validateIds(customerId, parcelId);
            Customer customer = findCustomer(customerId);
            Parcel parcel = parcelMap.getParcel(parcelId);

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