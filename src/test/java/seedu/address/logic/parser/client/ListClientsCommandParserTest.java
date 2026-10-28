package seedu.address.logic.parser.client;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.client.ListClientsCommand;

public class ListClientsCommandParserTest {
    //@@author anderschow
    private final ListClientsCommandParser parser = new ListClientsCommandParser();
    //@@author

    //@@author anderschow
    @Test
    public void parse_validQuery_success() {
        assertParseSuccess(parser, "  ", new ListClientsCommand());
        for (String keyword : new String[]{"a", "a".repeat(60), "O'Neil-Jr. @mail #12", "Amelia Tan",
            "sam+grooming", "10 Sunset Road,", "a_b", "a/b", "a!", "\u00e9", "$%^&*()=?:;[]{}"}) {
            assertParseSuccess(parser, " q/" + keyword + "  ", new ListClientsCommand(keyword));
        }
    }

    @Test
    public void parse_invalidQuery_failure() {
        for (String keyword : new String[]{"", "   ", "a".repeat(61), "a\tb", "a\nb", "a\u0000b", "a\u007fb"}) {
            assertParseFailure(parser, " q/" + keyword, ListClientsCommand.MESSAGE_INVALID_KEYWORD);
        }
        assertParseFailure(parser, " q/amelia q/bob", ListClientsCommand.MESSAGE_DUPLICATE_QUERY);
        assertParseFailure(parser, " q/ q/", ListClientsCommand.MESSAGE_DUPLICATE_QUERY);
        assertParseFailure(parser, "amelia", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                ListClientsCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "n/amelia", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                ListClientsCommand.MESSAGE_USAGE));
    }
    //@@author
}
