package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.model.person.Phone;

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
        assertParseSuccess(parser, "1", new DeleteClientCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_validPhoneIdentifier_returnsDeleteCommand() {
        assertParseSuccess(parser, "i/91234567", new DeleteClientCommand(new Phone("91234567")));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_indexAndPhoneIdentifier_throwsParseException() {
        assertParseFailure(parser, "1 i/91234567",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE));
    }
}
