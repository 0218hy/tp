package seedu.address.logic.parser.client;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.client.FindClientsCommand;
import seedu.address.model.client.ClientNameContainsKeywordsPredicate;

public class FindClientsCommandParserTest {

    private FindClientsCommandParser parser = new FindClientsCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindClientsCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindClientsCommand() {
        // no leading and trailing whitespaces
        FindClientsCommand expectedFindClientsCommand =
                new FindClientsCommand(new ClientNameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindClientsCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindClientsCommand);
    }

}
