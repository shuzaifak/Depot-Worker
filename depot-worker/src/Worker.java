class Worker {
    private static final double MAX_FEE = 1000.0;

    public double calculateFee(Parcel parcel) throws ValidationException {
        if (parcel == null) {
            throw new ValidationException("Cannot calculate fee for null parcel");
        }
        if (parcel.isProcessed()) {
            throw new ValidationException("Parcel has already been processed");
        }

        double baseFee = 10.0;
        double weightFee = parcel.getWeight() * 0.5;
        double typeFee = switch (parcel.getType().toLowerCase()) {
            case "fragile" -> 5.0;
            case "perishable" -> 7.0;
            case "standard" -> 0.0;
            default -> throw new ValidationException("Invalid parcel type");
        };

        double totalFee = baseFee + weightFee + typeFee;
        if (totalFee > MAX_FEE) {
            throw new ValidationException("Calculated fee exceeds maximum allowed amount");
        }
        return totalFee;
    }

    public void processParcel(Parcel parcel) throws ValidationException {
        double fee = calculateFee(parcel);
        parcel.setFee(fee);
        parcel.setProcessed(true);
        Log.getInstance().addEntry("Processed parcel " + parcel.getId() + 
                                 " with fee: $" + String.format("%.2f", fee));
    }
}
