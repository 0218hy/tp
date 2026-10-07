package seedu.address.model.pet.exceptions;

/**
 * Signals that the operation will result in duplicate pets (pets are considered duplicates if they have the same
 * owner and name).
 */
public class DuplicatePetException extends RuntimeException {

    public DuplicatePetException() {
        super("Operation would result in duplicate pets");
    }
}
