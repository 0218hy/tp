package seedu.address.logic.parser.client;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_QUERY;

import seedu.address.logic.commands.client.ListClientsCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the optional client search keyword. */
public class ListClientsCommandParser implements Parser<ListClientsCommand> {
    //@@author anderschow
    /**
     * Parses an optional {@code q/} keyword for the list-clients command.
     *
     * @param args Command arguments, excluding the command word.
     * @return A command that lists all clients or searches using the trimmed keyword.
     * @throws ParseException if a query is repeated, the keyword is invalid, or a preamble is present.
     */
    @Override
    public ListClientsCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.strip(), PREFIX_QUERY);
        if (arguments.getAllValues(PREFIX_QUERY).size() > 1) {
            throw new ParseException(ListClientsCommand.MESSAGE_DUPLICATE_QUERY);
        }
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListClientsCommand.MESSAGE_USAGE));
        }
        if (arguments.getValue(PREFIX_QUERY).isEmpty()) {
            return new ListClientsCommand();
        }
        String keyword = arguments.getValue(PREFIX_QUERY).get();
        if (keyword.isBlank() || keyword.length() > 60
                || keyword.codePoints().anyMatch(Character::isISOControl)) {
            throw new ParseException(ListClientsCommand.MESSAGE_INVALID_KEYWORD);
        }
        return new ListClientsCommand(keyword);
    }
    //@@author
}
