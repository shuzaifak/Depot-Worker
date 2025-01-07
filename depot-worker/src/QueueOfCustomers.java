import java.util.*;
class QueueOfCustomers {
    private Queue<Customer> customerQueue;
    private static final int MAX_QUEUE_SIZE = 100;

    public QueueOfCustomers() {
        customerQueue = new LinkedList<>();
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
