package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_IDENTIFIER;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteClientCommand object
 */
public class DeleteClientCommandParser implements Parser<DeleteClientCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteClientCommand
     * and returns a DeleteClientCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteClientCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_IDENTIFIER);
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_IDENTIFIER);

            boolean hasIndex = !argMultimap.getPreamble().isEmpty();
            boolean hasPhone = argMultimap.getValue(PREFIX_IDENTIFIER).isPresent();

            if (hasIndex == hasPhone) {
                throw new ParseException("Exactly one index or phone identifier must be provided.");
            }
            if (hasIndex) {
                Index index = ParserUtil.parseIndex(args);
                return new DeleteClientCommand(index);
            }
            return new DeleteClientCommand(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_IDENTIFIER).get()));
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE), pe);
        }
    }

}
