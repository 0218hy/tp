package seedu.address.model.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Client's name in the client book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class ClientName {

    public static final String MESSAGE_CONSTRAINTS =
            "Client names must be 2-60 characters and use valid name characters.";

    public final String fullName;

    /**
     * Constructs a {@code ClientName}.
     *
     * @param name A valid name.
     */
    public ClientName(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        if (!(test.length() >= 2 && test.length() <= 60)) {
            return false;
        }
        return !test.isBlank() && test.matches("[A-Za-z .'-]+");
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ClientName otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
