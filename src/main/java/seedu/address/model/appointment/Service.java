package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

/**
 * The grooming services supported by the MVP.
 */
public enum Service {
    BATH_AND_BRUSH("Bath and brush"),
    FULL_GROOM("Full groom"),
    NAIL_TRIM("Nail trim"),
    OTHER("Other");

    public static final String MESSAGE_CONSTRAINTS =
            "Service must be Bath and brush, Full groom, Nail trim or Other.";

    private final String displayName;

    Service(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Parses a service label, ignoring case and leading, trailing or repeated whitespace.
     * Unrecognised services are rejected rather than silently mapped to Other.
     */
    public static Service fromString(String input) {
        requireNonNull(input);
        String normalised = input.trim().replaceAll("\\s+", " ");
        for (Service service : values()) {
            if (service.displayName.equalsIgnoreCase(normalised)) {
                return service;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
