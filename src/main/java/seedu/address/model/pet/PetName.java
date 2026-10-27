package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a pet's name in the BuBu.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}.
 */
public class PetName {

    public static final String MESSAGE_CONSTRAINTS =
            "Pet name must be 1-40 characters and use valid name characters.";
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N} .'-]+";
    private static final int MAX_LENGTH = 40;

    public final String value;

    /**
     * Constructs a {@code PetName}.
     *
     * @param name A valid pet name.
     */
    public PetName(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        value = name;
    }

    // @@author xinyu-zxy
    /**
     * Returns whether the text is non-null, non-blank, at most 40 characters, and contains only letters,
     * numbers, spaces, periods, apostrophes, or hyphens.
     */
    public static boolean isValidName(String test) {
        return test != null && !test.isBlank() && test.length() <= MAX_LENGTH
                && test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns the form used to identify duplicate names under one owner.
     * Leading and trailing whitespace is removed, internal whitespace is collapsed,
     * and letter case is ignored.
     */
    public String getNormalizedName() {
        String trimmedName = value.strip();
        String collapsedName = trimmedName.replaceAll("\\s+", " ");
        String normalizedName = collapsedName.toLowerCase(Locale.ROOT);
        assert !normalizedName.isBlank() : "Normalizing a valid pet name must not produce a blank name";
        return normalizedName;
    }
    // @@author

    /** Returns whether this name matches {@code otherName} after normalization. */
    public boolean hasSameName(PetName otherName) {
        requireNonNull(otherName);
        return getNormalizedName().equals(otherName.getNormalizedName());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PetName otherPetName)) {
            return false;
        }

        return value.equals(otherPetName.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
