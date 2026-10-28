package seedu.address.model.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Client's email in the BuBu.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    //@@author anderschow
    public static final int MAX_LENGTH = 254;
    public static final String MESSAGE_CONSTRAINTS =
            "Email must be at most " + MAX_LENGTH
                    + " characters, contain one @, no spaces, and a domain with a full stop.";

    // Unicode character classes ensure that \s also rejects non-ASCII whitespace and space separators.
    public static final String VALIDATION_REGEX =
            "(?U)[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+";
    //@@author

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    //@@author anderschow
    /**
     * Returns true if a given string is a valid email.
     * Bounds input before regex matching to prevent stack exhaustion on oversized domains.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }
    //@@author

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
