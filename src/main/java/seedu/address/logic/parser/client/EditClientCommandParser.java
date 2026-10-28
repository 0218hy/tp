package seedu.address.logic.parser.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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

import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.EditClientCommand.EditClientDescriptor;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditClientCommand object
 */
public class EditClientCommandParser implements Parser<EditClientCommand> {

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

        if (!argMultimap.getPreamble().isEmpty()
                || argMultimap.getValue(PREFIX_IDENTIFIER).orElse("").isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditClientCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_IDENTIFIER, PREFIX_NAME, PREFIX_NEW_PHONE,
                PREFIX_EMAIL, PREFIX_ADDRESS);
        Phone identifier = ClientParserUtil.parsePhone(argMultimap.getValue(PREFIX_IDENTIFIER).orElseThrow());

        EditClientDescriptor editClientDescriptor = new EditClientDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editClientDescriptor.setName(ClientParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_NEW_PHONE).isPresent()) {
            editClientDescriptor.setPhone(ClientParserUtil.parsePhone(argMultimap.getValue(PREFIX_NEW_PHONE).get()));
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

        return new EditClientCommand(identifier, editClientDescriptor);
    }
    //@@author

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ClientParserUtil.parseTags(tagSet));
    }

}
