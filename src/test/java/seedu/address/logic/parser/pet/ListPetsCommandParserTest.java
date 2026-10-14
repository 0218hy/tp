package seedu.address.logic.parser.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.pet.ListPetsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Phone;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;

/**
 * Tests input parsing for {@code list-pets}.
 */
public class ListPetsCommandParserTest {
    private final ListPetsCommandParser parser = new ListPetsCommandParser();

    @Test
    public void parse_withoutOwner_createsAllPetsCommand() throws Exception {
        assertEquals(new ListPetsCommand(null), parser.parse(""));
        assertEquals(new ListPetsCommand(null), parser.parse("   "));
    }

    @Test
    public void parse_validOwner_createsFilteredCommand() throws Exception {
        assertEquals(new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(new Phone("91234567"))),
                parser.parse(" i/91234567 "));
        assertEquals(new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(new Phone("81234567"))),
                parser.parse("i/81234567"));
    }

    @Test
    public void parse_invalidPhone_throwsParseException() {
        String message = ListPetsCommandParser.MESSAGE_INVALID_OWNER;
        for (String phone : new String[] {"71234567", "9123456", "912345678", "9123-4567", "9123 4567", ""}) {
            assertThrows(ParseException.class, message, () -> parser.parse("i/" + phone));
        }
    }

    @Test
    public void parse_repeatedOwner_throwsParseException() {
        String message = ListPetsCommandParser.MESSAGE_DUPLICATE_OWNER;
        assertThrows(ParseException.class, message, () -> parser.parse("i/91234567 i/81234567"));
        assertThrows(ParseException.class, message, () -> parser.parse("i/91234567 i/"));
    }

    @Test
    public void parse_unknownPrefixOrExtraText_throwsParseException() {
        String message = ListPetsCommandParser.MESSAGE_INVALID_FORMAT;
        String[] invalidInputs = {"p/Mochi", "i/91234567 p/Mochi", "unexpected i/91234567",
            "i/91234567 unexpected", "unexpected"};
        for (String input : invalidInputs) {
            assertThrows(ParseException.class, message, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
