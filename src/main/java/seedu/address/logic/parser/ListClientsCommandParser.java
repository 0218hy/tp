package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_QUERY;

import seedu.address.logic.commands.ListClientsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the optional client search keyword. */
public class ListClientsCommandParser implements Parser<ListClientsCommand> {
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
        if (!keyword.matches("[A-Za-z0-9 '@#.\\-]{1,60}") || keyword.isBlank()) {
            throw new ParseException(ListClientsCommand.MESSAGE_INVALID_KEYWORD);
        }
        return new ListClientsCommand(keyword);
    }
}
