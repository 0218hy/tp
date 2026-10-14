package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a pet's care requirement in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidRequirement(String)}.
 */
public class Requirement {

    public static final String MESSAGE_CONSTRAINTS =
            "Requirements must be 5-240 characters and cannot contain /.";
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 240;
    private static final String UNSUPPORTED_CHARACTER = "/";

    public final String value;

    /**
     * Constructs a {@code Requirement}.
     *
     * @param requirement A valid pet care requirement.
     */
    public Requirement(String requirement) {
        requireNonNull(requirement);
        checkArgument(isValidRequirement(requirement), MESSAGE_CONSTRAINTS);
        value = requirement;
    }

    /**
     * Returns whether the text is a valid pet care requirement.
     * Requirements must fit the length limit and cannot contain a slash.
     */
    public static boolean isValidRequirement(String test) {
        return test != null && !test.isBlank() && test.length() >= MIN_LENGTH
                && test.length() <= MAX_LENGTH && !test.contains(UNSUPPORTED_CHARACTER);
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

        if (!(other instanceof Requirement otherRequirement)) {
            return false;
        }

        return value.equals(otherRequirement.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
