package seedu.address.logic.parser.client;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_TAG;

//@@author anderschow
import java.util.Map;
//@@author
import java.util.Set;

import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Address;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientName;
import seedu.address.model.client.Email;
import seedu.address.model.client.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddClientCommand object
 */
public class AddClientCommandParser implements Parser<AddClientCommand> {

    //@@author anderschow
    private static final Map<Prefix, String> FIELD_NAMES = Map.of(
            PREFIX_NAME, "Name", PREFIX_PHONE, "Phone number", PREFIX_EMAIL, "Email", PREFIX_ADDRESS, "Address");
    private static final Map<Prefix, String> FIELD_EXAMPLES = Map.of(
            PREFIX_NAME, "Alice Tan", PREFIX_PHONE, "91234567",
            PREFIX_EMAIL, "alice@example.com", PREFIX_ADDRESS, "123 Test Road");
    //@@author

    //@@author anderschow
    /**
     * Parses the given {@code String} of arguments in the context of the AddClientCommand
     * and returns an AddClientCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddClientCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddClientCommand.MESSAGE_USAGE));
        }

        Prefix[] requiredPrefixes = {
            PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS
        };
        for (Prefix prefix : requiredPrefixes) {
            if (argMultimap.getAllValues(prefix).size() > 1) {
                throw new ParseException(
                        fieldLabel(prefix) + " was supplied more than once. Keep only one " + prefix + " field.");
            }
        }
        for (Prefix prefix : requiredPrefixes) {
            if (argMultimap.getValue(prefix).isEmpty()) {
                throw new ParseException(fieldLabel(prefix) + " is required. Example: "
                        + prefix + FIELD_EXAMPLES.get(prefix) + ".");
            }
            if (argMultimap.getValue(prefix).orElseThrow().isBlank()) {
                throw new ParseException(fieldLabel(prefix) + " cannot be blank.");
            }
        }
        ClientName name = ClientParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ClientParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ClientParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ClientParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        Set<Tag> tagList = ClientParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Client client = new Client(name, phone, email, address, tagList);

        return new AddClientCommand(client);
    }
    //@@author
    /** Uses the same field label for absent, blank and repeated input. */
    //@@author anderschow
    private static String fieldLabel(Prefix prefix) {
        return FIELD_NAMES.get(prefix) + " (" + prefix + ")";
    }
    //@@author
}
