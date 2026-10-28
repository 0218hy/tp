package seedu.address.logic.parser.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_IDENTIFIER;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteClientCommand object
 */
public class DeleteClientCommandParser implements Parser<DeleteClientCommand> {

    public static final String MESSAGE_INDEX_AND_IDENTIFIER =
            "Choose either a client index or a phone identifier, not both.";
    public static final String MESSAGE_TARGET_REQUIRED =
            "Choose a client index or provide i/PHONE_NUMBER.";
    public static final String MESSAGE_INVALID_INDEX = "Client index must be a positive whole number.";

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z]+/)");

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteClientCommand
     * and returns a DeleteClientCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteClientCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(args);
        while (parameterMatcher.find()) {
            String parameter = parameterMatcher.group(1);
            if (!isKnownParameter(parameter)) {
                throw new ParseException(Messages.getErrorMessageForUnknownParameter(
                        parameter, DeleteClientCommand.MESSAGE_USAGE));
            }
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_IDENTIFIER);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_IDENTIFIER);

        boolean hasIndex = !argMultimap.getPreamble().isEmpty();
        boolean hasPhone = argMultimap.getValue(PREFIX_IDENTIFIER).isPresent();

        if (!hasIndex && !hasPhone) {
            throw new ParseException(MESSAGE_TARGET_REQUIRED);
        }
        if (hasIndex && hasPhone) {
            throw new ParseException(MESSAGE_INDEX_AND_IDENTIFIER);
        }
        if (hasIndex) {
            return new DeleteClientCommand(parseIndex(argMultimap.getPreamble()));
        }
        return new DeleteClientCommand(ClientParserUtil.parsePhone(
                argMultimap.getValue(PREFIX_IDENTIFIER).orElseThrow()));
    }

    /** Parses a client-list index with a client-specific error message. */
    private static Index parseIndex(String value) throws ParseException {
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INDEX, e);
        }
    }

    /** Returns whether the supplied parameter is supported by this command. */
    private static boolean isKnownParameter(String parameter) {
        Prefix prefix = PREFIX_IDENTIFIER;
        return prefix.toString().equals(parameter);
    }

}
