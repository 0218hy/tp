package seedu.address.model.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

//@@author anderschow
import java.text.Normalizer;
//@@author

/**
 * Represents a Client's name in the BuBu.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class ClientName {

    //@@author anderschow
    public static final String MESSAGE_CONSTRAINTS =
            "Client names must be nonblank and contain 2-60 Unicode code points after NFC normalization. "
            + "Use Unicode letters, combining marks, spaces, apostrophes, hyphens or full stops.";
    //@@author

    public final String fullName;

    /**
     * Constructs a {@code ClientName}.
     *
     * @param name A valid name.
     */
    public ClientName(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        //@@author anderschow
        fullName = Normalizer.normalize(name, Normalizer.Form.NFC);
        //@@author
    }

    //@@author anderschow
    /**
     * Returns true if a given string is a valid name after NFC normalization.
     * Length is measured in Unicode code points, including spaces, punctuation and combining marks.
     */
    public static boolean isValidName(String test) {
        String normalized = Normalizer.normalize(test, Normalizer.Form.NFC);
        int length = normalized.codePointCount(0, normalized.length());
        if (length < 2 || length > 60) {
            return false;
        }
        return !normalized.isBlank() && normalized.matches("[\\p{L}\\p{M} .'-]+");
    }
    //@@author


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
