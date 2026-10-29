package seedu.address.logic.parser.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.pet.ListPetsCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;

// @@author xinyu-zxy
/**
 * Parses the optional owner phone number for {@code list-pets}.
 */
public class ListPetsCommandParser implements Parser<ListPetsCommand> {
    public static final String MESSAGE_DUPLICATE_OWNER = "The owner phone may be specified only once.";
    public static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListPetsCommand.MESSAGE_USAGE);
    public static final String MESSAGE_INVALID_OWNER = Phone.MESSAGE_CONSTRAINTS;
    private static final int MAX_OWNER_FIELDS = 1;

    /**
     * Matches command-style, single-letter prefixes so unsupported fields are not mistaken for phone text.
     */
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)[a-zA-Z]/");

    /**
     * Separates a valid phone followed by stray text from a malformed phone number.
     */
    private static final Pattern EXTRA_TEXT_AFTER_PHONE_PATTERN =
            Pattern.compile(Phone.VALIDATION_REGEX + "\\s+.+");

    /**
     * Parses an optional owner identifier and rejects repeated or unsupported fields.
     *
     * @param args Non-null text following the {@code list-pets} command word.
     * @return A command for all pets or for the specified owner's pets.
     * @throws ParseException if the command structure or phone number is invalid.
     */
    @Override
    public ListPetsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String input = args.trim();
        if (input.isEmpty()) {
            return new ListPetsCommand(null);
        }

        // The tokenizer recognizes a prefix only when whitespace precedes it, including the first prefix.
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + input, PREFIX_OWNER_IDENTIFIER);
        validateStructure(input, fields);
        Phone ownerPhone = parseOwnerPhone(fields);
        PetOwnerPhoneMatchesPredicate ownerFilter = new PetOwnerPhoneMatchesPredicate(ownerPhone);
        return new ListPetsCommand(ownerFilter);
    }

    /**
     * Rejects extra text, unsupported prefixes, and repeated owner identifiers.
     *
     * @param input The trimmed command arguments to check for unknown prefixes.
     * @param fields The tokenized owner identifier fields.
     * @throws ParseException if the command structure is invalid.
     */
    private void validateStructure(String input, ArgumentMultimap fields) throws ParseException {
        if (!fields.getPreamble().isEmpty() || hasUnknownPrefix(input)) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (fields.getAllValues(PREFIX_OWNER_IDENTIFIER).size() > MAX_OWNER_FIELDS) {
            throw new ParseException(MESSAGE_DUPLICATE_OWNER);
        }
    }

    /**
     * Parses the owner identifier as a valid Singapore mobile phone number.
     * Repeated owner identifiers must have been rejected by structure validation.
     *
     * @param fields The tokenized owner identifier fields.
     * @throws ParseException if the phone number is invalid or followed by extra text.
     */
    private Phone parseOwnerPhone(ArgumentMultimap fields) throws ParseException {
        assert fields.getAllValues(PREFIX_OWNER_IDENTIFIER).size() <= MAX_OWNER_FIELDS
                : "Repeated owner identifiers must be rejected before parsing the phone";
        String phoneValue = fields.getValue(PREFIX_OWNER_IDENTIFIER)
                .orElseThrow(() -> new ParseException(MESSAGE_INVALID_FORMAT));
        if (EXTRA_TEXT_AFTER_PHONE_PATTERN.matcher(phoneValue).matches()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (!Phone.isValidPhone(phoneValue)) {
            throw new ParseException(MESSAGE_INVALID_OWNER);
        }
        return new Phone(phoneValue);
    }

    /**
     * Returns whether the input has a parameter prefix other than {@code i/}.
     *
     * @param input The command arguments to inspect for unsupported prefixes.
     */
    private boolean hasUnknownPrefix(String input) {
        Matcher matcher = PREFIX_PATTERN.matcher(input);
        while (matcher.find()) {
            String prefix = matcher.group().trim();
            if (!prefix.equals(PREFIX_OWNER_IDENTIFIER.toString())) {
                return true;
            }
        }
        return false;
    }
}
