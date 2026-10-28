package seedu.address.logic.parser.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_IDENTIFIER;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NEW_PHONE;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

//@@author anderschow
import seedu.address.commons.core.index.Index;
//@@author
import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.EditClientCommand.EditClientDescriptor;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
//@@author anderschow
import seedu.address.logic.parser.ParserUtil;
//@@author
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditClientCommand object
 */
public class EditClientCommandParser implements Parser<EditClientCommand> {

    //@@author anderschow
    public static final String MESSAGE_MISSING_TARGET =
            "Missing client selector. Use INDEX or i/CURRENT_PHONE.";
    public static final String MESSAGE_BLANK_IDENTIFIER =
            "Current phone selector (i/) is blank. Use i/CURRENT_PHONE.";
    public static final String MESSAGE_MIXED_TARGETS =
            "Choose either INDEX or i/CURRENT_PHONE, not both.";
    public static final String MESSAGE_UNEXPECTED_TEXT =
            "Unexpected text before fields. Use a positive client INDEX or i/CURRENT_PHONE.";
    public static final String MESSAGE_INVALID_CURRENT_PHONE =
            "Current phone number (i/) must be eight digits beginning with 8 or 9.";
    public static final String MESSAGE_INVALID_NEW_PHONE =
            "New phone number (p/) must be eight digits beginning with 8 or 9.";
    //@@author

    //@@author anderschow
    /**
     * Parses the given {@code String} of arguments in the context of the EditClientCommand
     * and returns an EditClientCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditClientCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_IDENTIFIER, PREFIX_NAME,
                        PREFIX_NEW_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

        boolean hasIndex = !argMultimap.getPreamble().isEmpty();
        boolean hasIdentifier = argMultimap.getValue(PREFIX_IDENTIFIER).isPresent();
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_IDENTIFIER, PREFIX_NAME, PREFIX_NEW_PHONE,
                PREFIX_EMAIL, PREFIX_ADDRESS);
        String preamble = argMultimap.getPreamble();
        if (hasIndex && !preamble.matches("[+-]?[0-9]+(?:[.][0-9]+)?")) {
            throw new ParseException(MESSAGE_UNEXPECTED_TEXT);
        }
        if (hasIndex && hasIdentifier) {
            throw new ParseException(MESSAGE_MIXED_TARGETS);
        }
        if (!hasIndex && !hasIdentifier) {
            throw new ParseException(MESSAGE_MISSING_TARGET);
        }
        if (hasIdentifier && argMultimap.getValue(PREFIX_IDENTIFIER).orElseThrow().isBlank()) {
            throw new ParseException(MESSAGE_BLANK_IDENTIFIER);
        }
        Index index = hasIndex ? ParserUtil.parseIndex(preamble) : null;
        Phone identifier = hasIdentifier
                ? parsePhone(argMultimap.getValue(PREFIX_IDENTIFIER).orElseThrow(), MESSAGE_INVALID_CURRENT_PHONE)
                : null;

        EditClientDescriptor editClientDescriptor = new EditClientDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editClientDescriptor.setName(ClientParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_NEW_PHONE).isPresent()) {
            editClientDescriptor.setPhone(parsePhone(argMultimap.getValue(PREFIX_NEW_PHONE).get(),
                    MESSAGE_INVALID_NEW_PHONE));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editClientDescriptor.setEmail(ClientParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editClientDescriptor.setAddress(ClientParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editClientDescriptor::setTags);

        if (!editClientDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditClientCommand.MESSAGE_NOT_EDITED);
        }

        return hasIndex ? new EditClientCommand(index, editClientDescriptor)
                : new EditClientCommand(identifier, editClientDescriptor);
    }
    //@@author

    /** Adds selector/update context to phone validation without exposing the supplied value. */
    //@@author anderschow
    private static Phone parsePhone(String value, String errorMessage) throws ParseException {
        try {
            return ClientParserUtil.parsePhone(value);
        } catch (ParseException e) {
            throw new ParseException(errorMessage, e);
        }
    }
    //@@author

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        //@@author anderschow
        assert tags != null : "Argument lookup must return a non-null tag collection, even when no tags are supplied";
        //@@author

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ClientParserUtil.parseTags(tagSet));
    }

}
