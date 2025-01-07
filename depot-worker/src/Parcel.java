// Parcel.java
class Parcel {
    private String id;
    private double weight;
    private String type;
    private boolean processed;
    private double fee;
    private ParcelStatus status;
    private String assignedCustomerId;

    public Parcel(String id, double weight, String type) throws ValidationException {
        validateParcelData(id, weight, type);
        this.id = id;
        this.weight = weight;
        this.type = type;
        this.processed = false;
        this.fee = 0.0;
        this.status = ParcelStatus.IN_WAREHOUSE;
        this.assignedCustomerId = null;
    }

 

    private void validateParcelData(String id, double weight, String type) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Parcel ID cannot be empty");
        }
        if (!id.matches("P\\d{3}")) {
            throw new ValidationException("Parcel ID must be in format P followed by 3 digits");
        }
        if (weight <= 0 || weight > 100) {
            throw new ValidationException("Weight must be between 0 and 100 kg");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException("Parcel type cannot be empty");
        }
        String normalizedType = type.toLowerCase();
        if (!normalizedType.equals("standard") && 
            !normalizedType.equals("fragile") && 
            !normalizedType.equals("perishable")) {
            throw new ValidationException("Invalid parcel type. Must be Standard, Fragile, or Perishable");
        }
    }

    // Getters and setters
    public String getId() { return id; }
    public double getWeight() { return weight; }
    public String getType() { return type; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public ParcelStatus getStatus() { return status; }
    public void setStatus(ParcelStatus status) { this.status = status; }
    public String getAssignedCustomerId() { return assignedCustomerId; }
    public void setAssignedCustomerId(String customerId) { this.assignedCustomerId = customerId; }

    @Override
    public String toString() {
        return String.format("Parcel[id=%s, weight=%.2f, type=%s, status=%s, fee=%.2f]",
                           id, weight, type, status, fee);
    }
}
