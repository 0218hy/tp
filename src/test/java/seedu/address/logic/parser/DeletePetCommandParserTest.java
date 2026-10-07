package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeletePetCommand;
import seedu.address.model.person.Phone;
import seedu.address.model.pet.PetName;

public class DeletePetCommandParserTest {

    private final DeletePetCommandParser parser = new DeletePetCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        assertParseSuccess(parser, " p/Milo i/98765432",
                new DeletePetCommand(new PetName("Milo"), new Phone("98765432")));
        assertParseSuccess(parser, " p/Milo the dog i/98765432 ",
                new DeletePetCommand(new PetName("Milo the dog"), new Phone("98765432")));
        assertParseSuccess(parser, " i/98765432 p/Milo",
                new DeletePetCommand(new PetName("Milo"), new Phone("98765432")));
    }

    @Test
    public void parse_missingOrUnexpectedFields_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeletePetCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " p/Milo", expectedMessage);
        assertParseFailure(parser, " i/98765432", expectedMessage);
        assertParseFailure(parser, " unexpected p/Milo i/98765432", expectedMessage);
    }

    @Test
    public void parse_duplicateFields_failure() {
        assertParseFailure(parser, " p/Milo p/Luna i/98765432",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PET_NAME));
        assertParseFailure(parser, " p/Milo i/98765432 i/87654321",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_OWNER_IDENTIFIER));
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " p/M@lo i/98765432", PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " p/ i/98765432", PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " p/Milo i/123a", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " p/Milo i/", Phone.MESSAGE_CONSTRAINTS);
    }
}
