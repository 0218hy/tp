//@@author anderschow
package seedu.address.logic.commands.client;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.client.Client;

/** Checks both unique contact fields across all saved clients before any records are changed. */
final class ClientConflictCheck {
    private ClientConflictCheck() {}

    /** Excludes the edit target; pass null when adding a new client. */
    static void validate(List<Client> savedClients, Client proposed, Client target) throws CommandException {
        boolean phoneConflict = false;
        boolean emailConflict = false;
        for (Client saved : savedClients) {
            if (saved.equals(target)) {
                continue;
            }
            phoneConflict |= saved.getPhone().equals(proposed.getPhone());
            emailConflict |= saved.getEmail().equals(proposed.getEmail());
        }
        if (phoneConflict && emailConflict) {
            throw new CommandException(AddClientCommand.MESSAGE_DUPLICATE_CLIENT);
        }
        if (phoneConflict) {
            throw new CommandException(AddClientCommand.MESSAGE_DUPLICATE_PHONE);
        }
        if (emailConflict) {
            throw new CommandException(AddClientCommand.MESSAGE_DUPLICATE_EMAIL);
        }
    }
}
//@@author
