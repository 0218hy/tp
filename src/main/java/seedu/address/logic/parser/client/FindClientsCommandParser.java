package seedu.address.logic.parser.client;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.client.FindClientsCommand;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.ClientNameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindClientsCommand object
 */
public class FindClientsCommandParser implements Parser<FindClientsCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindClientsCommand
     * and returns a FindClientsCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindClientsCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindClientsCommand.MESSAGE_USAGE));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindClientsCommand(new ClientNameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
