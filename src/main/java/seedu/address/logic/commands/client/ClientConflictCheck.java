//@@author anderschow
package seedu.address.logic.commands.client;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.client.Client;

/** Checks both unique contact fields across all saved clients before any records are changed. */
final class ClientConflictCheck {
    //@@author chunghean
    static final String MESSAGE_HISTORICAL_PHONE_CONFLICT =
            "The phone number is already used by a different historical appointment owner.";
    //@@author

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

    //@@author chunghean
    /**
     * Validates a client against registered clients and historical appointment owner snapshots.
     *
     * <p>A phone number is checked against appointment snapshots only when it is newly introduced. This
     * preserves existing edits that do not change the client's phone number.</p>
     */
    static void validate(ReadOnlyBubuBook bubuBook, Client proposed, Client target) throws CommandException {
        validate(bubuBook.getClientList(), proposed, target);
        boolean phoneChanged = target == null || !target.getPhone().equals(proposed.getPhone());
        if (phoneChanged && bubuBook.hasAppointmentOwnerConflict(proposed)) {
            throw new CommandException(MESSAGE_HISTORICAL_PHONE_CONFLICT);
        }
    }
    //@@author
}
//@@author
