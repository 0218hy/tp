package seedu.address.logic.parser.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.Messages.getErrorMessageForUnknownParameter;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_IDENTIFIER;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.model.client.Phone;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteClientCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteClientCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteClientCommandParserTest {

    private DeleteClientCommandParser parser = new DeleteClientCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteClientCommand(INDEX_FIRST_CLIENT));
    }

    //@@author chunghean
    @Test
    public void parse_validPhoneIdentifier_returnsDeleteCommand() {
        assertParseSuccess(parser, "i/91234567", new DeleteClientCommand(new Phone("91234567")));
    }
    //@@author

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a",
                DeleteClientCommandParser.MESSAGE_INVALID_INDEX);
    }

    //@@author chunghean
    @Test
    public void parse_indexAndPhoneIdentifier_throwsParseException() {
        assertParseFailure(parser, "1 i/91234567",
                DeleteClientCommandParser.MESSAGE_INDEX_AND_IDENTIFIER);
    }

    @Test
    public void parse_missingTarget_throwsParseException() {
        assertParseFailure(parser, "", DeleteClientCommandParser.MESSAGE_TARGET_REQUIRED);
    }

    @Test
    public void parse_invalidPhone_reportsPhoneConstraints() {
        assertParseFailure(parser, "i/123", Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateIdentifier_reportsDuplicatePrefix() {
        assertParseFailure(parser, "i/91234567 i/92345678",
                getErrorMessageForDuplicatePrefixes(PREFIX_IDENTIFIER));
    }

    @Test
    public void parse_unknownParameter_reportsUnknownParameter() {
        assertParseFailure(parser, "x/91234567",
                getErrorMessageForUnknownParameter("x/", DeleteClientCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
    //@@author
}
