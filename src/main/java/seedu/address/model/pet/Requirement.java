package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a pet's care requirement in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidRequirement(String)}.
 */
public class Requirement {

    public static final String MESSAGE_CONSTRAINTS = "Pet requirements should not be blank";
    public static final String VALIDATION_REGEX = "[^\\s].*";

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
     * Returns true if a given string is a valid pet care requirement.
     */
    public static boolean isValidRequirement(String test) {
        return test.matches(VALIDATION_REGEX);
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
