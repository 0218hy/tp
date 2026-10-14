package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a pet's breed when one is known.
 * A pet without breed information stores no {@code Breed} value.
 */
public class Breed {
    public static final String MESSAGE_CONSTRAINTS = "Breed must be 2-50 characters and use valid text characters.";
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;
    private static final String VALIDATION_REGEX = "[\\p{L}\\p{N} .'-]+";

    public final String value;

    /**
     * Creates a breed from text that meets the length and character constraints.
     */
    public Breed(String breed) {
        requireNonNull(breed);
        checkArgument(isValidBreed(breed), MESSAGE_CONSTRAINTS);
        value = breed;
    }

    /**
     * Returns whether the supplied text is a valid breed.
     * Blank text and values outside the allowed length are invalid.
     */
    public static boolean isValidBreed(String breed) {
        return breed != null && breed.length() >= MIN_LENGTH && breed.length() <= MAX_LENGTH
                && breed.matches(VALIDATION_REGEX) && !breed.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Breed otherBreed && value.equals(otherBreed.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
