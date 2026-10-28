//@@author anderschow
package seedu.address.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.parser.client.EditClientCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.client.Address;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientName;
import seedu.address.model.client.Email;
import seedu.address.model.client.Phone;

/** Produces log metadata without copying user input or command feedback into logs. */
final class CommandLog {
    private static final Set<String> COMMANDS = Set.of("add-client", "edit-client", "delete-client", "list-clients",
            "add-pet", "edit-pet", "delete-pet", "list-pets", "add-appointment", "edit-appointment",
            "delete-appointment", "list-appointments", "clear", "help", "exit");

    private CommandLog() {}

    static String commandType(String input) {
        String word = input.trim().split("\\s+", 2)[0];
        return COMMANDS.contains(word) ? word : "unknown-command";
    }

    static String rejectionReason(Exception exception) {
        String message = exception.getMessage();
        if (Messages.MESSAGE_UNKNOWN_COMMAND.equals(message)) {
            return "unknown command";
        }
        if (ClientName.MESSAGE_CONSTRAINTS.equals(message)) {
            return "invalid client name format";
        }
        if (Email.MESSAGE_CONSTRAINTS.equals(message)) {
            return "invalid email format";
        }
        if (Address.MESSAGE_CONSTRAINTS.equals(message)) {
            return "invalid address format";
        }
        if (EditClientCommandParser.MESSAGE_INVALID_CURRENT_PHONE.equals(message)) {
            return "invalid current phone format";
        }
        if (EditClientCommandParser.MESSAGE_INVALID_NEW_PHONE.equals(message)) {
            return "invalid new phone format";
        }
        if (Phone.MESSAGE_CONSTRAINTS.equals(message)) {
            return "invalid phone format";
        }
        if (LogicManager.MESSAGE_INVALID_DISPLAY_TYPE.equals(message)) {
            return "index requires the matching displayed list";
        }
        if (LogicManager.MESSAGE_CORRUPTED_RECORDS.equals(message)) {
            return "existing records could not be verified";
        }
        if (EditClientCommand.MESSAGE_CLIENT_NOT_FOUND.equals(message)) {
            return "client identifier not found";
        }
        if (EditClientCommand.MESSAGE_DUPLICATE_CLIENT.equals(message)) {
            return "phone and email already belong to saved clients";
        }
        if (EditClientCommand.MESSAGE_DUPLICATE_PHONE.equals(message)) {
            return "phone already belongs to another client";
        }
        if (EditClientCommand.MESSAGE_DUPLICATE_EMAIL.equals(message)) {
            return "email already belongs to another client";
        }
        if (EditClientCommand.MESSAGE_NOT_EDITED.equals(message)) {
            return "no update fields supplied";
        }
        if (Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX.equals(message)) {
            return "client index out of range";
        }
        return exception instanceof ParseException ? "invalid command syntax or field format"
                : "record validation or lookup failed";
    }

    /** Compares saved client values, logging only names of fields that actually changed. */
    static String changedClientFields(ReadOnlyBubuBook before, ReadOnlyBubuBook after) {
        if (before.getClientList().size() != after.getClientList().size()) {
            return "";
        }
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < before.getClientList().size(); i++) {
            Client oldClient = before.getClientList().get(i);
            Client newClient = after.getClientList().get(i);
            addChanged(fields, "name", oldClient.getName(), newClient.getName());
            addChanged(fields, "phone", oldClient.getPhone(), newClient.getPhone());
            addChanged(fields, "email", oldClient.getEmail(), newClient.getEmail());
            addChanged(fields, "address", oldClient.getAddress(), newClient.getAddress());
            addChanged(fields, "tags", oldClient.getTags(), newClient.getTags());
        }
        return fields.isEmpty() ? "" : "; changedFields=" + fields;
    }

    private static void addChanged(List<String> fields, String field, Object before, Object after) {
        if (!before.equals(after) && !fields.contains(field)) {
            fields.add(field);
        }
    }
}
//@@author
