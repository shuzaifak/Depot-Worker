import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
class ParcelMap {
    private Map<String, Parcel> parcels;
    private static final int MAX_PARCELS = 1000;

    public ParcelMap() {
        parcels = new HashMap<>();
    }

    public void addParcel(Parcel parcel) throws ValidationException {
        if (parcel == null) {
            throw new ValidationException("Parcel cannot be null");
        }
        if (parcels.size() >= MAX_PARCELS) {
            throw new ValidationException("Maximum parcel limit reached");
        }
        if (parcels.containsKey(parcel.getId())) {
            throw new ValidationException("Parcel with ID " + parcel.getId() + " already exists");
        }
        parcels.put(parcel.getId(), parcel);
    }

    public Parcel findParcel(String id) {
        return parcels.get(id);
    }

    public void updateParcelStatus(String id, ParcelStatus status) throws ValidationException {
        Parcel parcel = findParcel(id);
        if (parcel == null) {
            throw new ValidationException("Parcel not found: " + id);
        }
        parcel.setStatus(status);
    }

    public Collection<Parcel> getAllParcels() {
        return parcels.values();
    }

    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/" + filename))) {
            for (Parcel parcel : parcels.values()) {
                writer.printf("%s,%f,%s,%s,%f,%s%n",
                    parcel.getId(),
                    parcel.getWeight(),
                    parcel.getType(),
                    parcel.getStatus(),
                    parcel.getFee(),
                    parcel.getAssignedCustomerId() != null ? parcel.getAssignedCustomerId() : "NONE");
            }
        }
    }

    public void loadFromFile(String filename) throws IOException, ValidationException {
        parcels.clear();
        Path path = Paths.get("data", filename);
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                Parcel parcel = new Parcel(parts[0], Double.parseDouble(parts[1]), parts[2]);
                parcel.setStatus(ParcelStatus.valueOf(parts[3]));
                parcel.setFee(Double.parseDouble(parts[4]));
                if (!parts[5].equals("NONE")) {
                    parcel.setAssignedCustomerId(parts[5]);
                }
                addParcel(parcel);
            }
        }
    }
}