package seedu.address.logic.parser.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.PetName;

public class PetParserUtilTest {
    private static final String INVALID_PET_NAME = "M@lo";

    private static final String VALID_PET_NAME = "Milo";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parsePetName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PetParserUtil.parsePetName(null));
    }

    @Test
    public void parsePetName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> PetParserUtil.parsePetName(INVALID_PET_NAME));
    }

    @Test
    public void parsePetName_validValueWithoutWhitespace_returnsPetName() throws Exception {
        assertEquals(new PetName(VALID_PET_NAME), PetParserUtil.parsePetName(VALID_PET_NAME));
    }

    @Test
    public void parsePetName_validValueWithWhitespace_returnsTrimmedPetName() throws Exception {
        String petNameWithWhitespace = WHITESPACE + VALID_PET_NAME + WHITESPACE;
        assertEquals(new PetName(VALID_PET_NAME), PetParserUtil.parsePetName(petNameWithWhitespace));
    }
}
