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

    public Parcel getParcel(String id) {
        return parcels.get(id);
    }

    public Collection<Parcel> getAllParcels() {
        return parcels.values();
    }
}
