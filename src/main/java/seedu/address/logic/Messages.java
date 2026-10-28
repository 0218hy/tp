package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.client.Client;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    public static final String MESSAGE_MISSING_PARAMETER = "Missing parameter: %1$s";
    public static final String MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_CLIENTS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message indicating an unsupported parameter and the command format to use.
     */
    public static String getErrorMessageForUnknownParameter(String parameter, String commandUsage) {
        return String.format(MESSAGE_UNKNOWN_PARAMETER, parameter)
                + "\nUse the command format below:\n"
                + commandUsage;
    }

    /**
     * Returns an error message indicating a missing parameter and the command format to use.
     */
    public static String getErrorMessageForMissingParameter(String parameter, String commandUsage) {
        return String.format(MESSAGE_MISSING_PARAMETER, parameter)
                + "\nUse the command format below:\n"
                + commandUsage;
    }

    /**
     * Formats the {@code client} for display to the user.
     */
    public static String format(Client client) {
        final StringBuilder builder = new StringBuilder();
        builder.append(client.getName())
                .append("; Phone: ")
                .append(client.getPhone())
                .append("; Email: ")
                .append(client.getEmail())
                .append("; Address: ")
                .append(client.getAddress())
                .append("; Tags: ");
        client.getTags().forEach(builder::append);
        return builder.toString();
    }

}
