package seedu.address.logic.parser.pet;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.PetName;

/** Parses pet fields supplied to commands. */
public class PetParserUtil {
    /**
     * Parses a {@code String petName} into a {@code PetName}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given pet name is invalid.
     */
    public static PetName parsePetName(String petName) throws ParseException {
        requireNonNull(petName);
        String trimmedPetName = petName.trim();
        if (!PetName.isValidName(trimmedPetName)) {
            throw new ParseException(PetName.MESSAGE_CONSTRAINTS);
        }
        return new PetName(trimmedPetName);
    }
}
