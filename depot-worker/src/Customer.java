import java.time.LocalDateTime;
import java.util.*;

class Customer {
    private String id;
    private String name;
    private List<Parcel> parcels;
    private LocalDateTime joinTime;

    public Customer(String id, String name) throws ValidationException {
        validateCustomerData(id, name);
        this.id = id;
        this.name = name;
        this.parcels = new ArrayList<>();
        this.joinTime = LocalDateTime.now();
    }
    private void validateCustomerData(String id, String name) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be empty");
        }
        if (!id.matches("C\\d{3}")) {
            throw new ValidationException("Customer ID must be in format C followed by 3 digits");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Customer name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new ValidationException("Customer name must be between 2 and 50 characters");
        }
    }

    // Getters and methods
    public String getId() { return id; }
    public String getName() { return name; }
    public List<Parcel> getParcels() { return parcels; }
    public void addParcel(Parcel parcel) { parcels.add(parcel); }
      public LocalDateTime getJoinTime() { return joinTime; }
    
    @Override
    public String toString() {
        return String.format("Customer[id=%s, name=%s, parcels=%d]", 
                           id, name, parcels.size());
    }
}