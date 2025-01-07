import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

class QueueOfCustomers {
    private Queue<Customer> customerQueue;
    private static final int MAX_QUEUE_SIZE = 100;

    public QueueOfCustomers() {
        customerQueue = new LinkedList<>();
    }
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/" + filename))) {
            for (Customer customer : customerQueue) {
                writer.printf("%s,%s,%s%n",
                    customer.getId(),
                    customer.getName(),
                    customer.getJoinTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }
    }

    public void loadFromFile(String filename) throws IOException, ValidationException {
        customerQueue.clear();
        Path path = Paths.get("data", filename);
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                Customer customer = new Customer(parts[0], parts[1]);
                addCustomer(customer);
            }
        }
    }
    public void addCustomer(Customer customer) throws ValidationException {
        if (customer == null) {
            throw new ValidationException("Customer cannot be null");
        }
        if (customerQueue.size() >= MAX_QUEUE_SIZE) {
            throw new ValidationException("Queue has reached maximum capacity");
        }
        customerQueue.offer(customer);
    }

    public Customer removeCustomer() {
        return customerQueue.poll();
    }

    public Customer peekCustomer() {
        return customerQueue.peek();
    }

    public boolean isEmpty() {
        return customerQueue.isEmpty();
    }

    public int size() {
        return customerQueue.size();
    }
}
