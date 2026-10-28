package seedu.address.logic.parser.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.pet.ListPetsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;

// @@author xinyu-zxy
/**
 * Tests input parsing for {@code list-pets}.
 */
public class ListPetsCommandParserTest {
    private final ListPetsCommandParser parser = new ListPetsCommandParser();

    @Test
    public void parse_withoutOwner_createsAllPetsCommand() throws Exception {
        // EP: owner filter omitted
        // BVA: zero input characters
        assertEquals(new ListPetsCommand(null), parser.parse(""));

        // EP: input containing only whitespace
        assertEquals(new ListPetsCommand(null), parser.parse("   "));
    }

    @Test
    public void parse_validOwner_createsFilteredCommand() throws Exception {
        // EP: valid eight-digit owner phone with surrounding whitespace
        assertEquals(new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(new Phone("91234567"))),
                parser.parse(" i/91234567 "));

        // EP: valid eight-digit owner phone without surrounding whitespace
        // BVA: 8 digits (required phone length)
        assertEquals(new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(new Phone("81234567"))),
                parser.parse("i/81234567"));
    }

    @Test
    public void parse_invalidPhone_throwsParseException() {
        String message = ListPetsCommandParser.MESSAGE_INVALID_OWNER;

        // EP: eight-digit phone with an unsupported starting digit
        assertThrows(ParseException.class, message, () -> parser.parse("i/71234567"));

        // EP: phone shorter than the required length
        // BVA: 7 digits, immediately below the required 8 digits
        assertThrows(ParseException.class, message, () -> parser.parse("i/9123456"));

        // EP: phone longer than the required length
        // BVA: 9 digits, immediately above the required 8 digits
        assertThrows(ParseException.class, message, () -> parser.parse("i/912345678"));

        // EP: phone containing unsupported separators or whitespace
        assertThrows(ParseException.class, message, () -> parser.parse("i/9123-4567"));
        assertThrows(ParseException.class, message, () -> parser.parse("i/9123 4567"));

        // EP: empty owner-phone value
        // BVA: zero digits
        assertThrows(ParseException.class, message, () -> parser.parse("i/"));
    }

    @Test
    public void parse_repeatedOwner_throwsParseException() {
        String message = ListPetsCommandParser.MESSAGE_DUPLICATE_OWNER;

        // EP: owner field supplied twice with non-empty values
        // BVA: 2 owner fields, immediately above the maximum of 1
        assertThrows(ParseException.class, message, () -> parser.parse("i/91234567 i/81234567"));

        // EP: owner field repeated with an empty second value
        assertThrows(ParseException.class, message, () -> parser.parse("i/91234567 i/"));
    }

    @Test
    public void parse_unknownPrefixOrExtraText_throwsParseException() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListPetsCommand.MESSAGE_USAGE);

        // EP: unsupported prefix, extra text before/after a valid field, or unprefixed text
        String[] invalidInputs = {"p/Mochi", "i/91234567 p/Mochi", "unexpected i/91234567",
            "i/91234567 unexpected", "unexpected"};
        for (String input : invalidInputs) {
            assertThrows(ParseException.class, message, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        // EP: null command arguments
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
