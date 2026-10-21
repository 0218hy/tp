package seedu.address.model.pet;

/**
 * The supported species categories for pets.
 */
public enum Species {
    DOG("Dog"),
    CAT("Cat"),
    RABBIT("Rabbit"),
    GUINEA_PIG("Guinea pig"),
    OTHER("Other");

    private final String displayName;

    /**
     * Creates a species with the label shown in pet command results.
     *
     * @param displayName The readable name shown to users.
     */
    Species(String displayName) {
        assert displayName != null && !displayName.isBlank() : "Every species must have a display label";
        this.displayName = displayName;
    }

    /**
     * Returns the readable species name used when displaying pet profiles.
     */
    public String getDisplayName() {
        return displayName;
    }
}
