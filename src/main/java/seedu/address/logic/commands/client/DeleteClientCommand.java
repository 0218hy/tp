package seedu.address.logic.commands.client;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;

/**
 * Deletes a client identified using its displayed index or phone number from the BuBu.
 */
public class DeleteClientCommand extends Command {

    public static final String COMMAND_WORD = "delete-client";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes a person by displayed index or phone number.\n"
            + "Parameters: INDEX (must be a positive integer), or i/PHONE_NUMBER\n"
            + "Examples: " + COMMAND_WORD + " 1\n"
            + "          " + COMMAND_WORD + " i/91234567";

    public static final String MESSAGE_DELETE_CLIENT_SUCCESS =
            "Deleted client: %1$s. Removed %2$s and %3$s.";
    public static final String MESSAGE_CLIENT_NOT_FOUND =
            "No client with the given phone number exists.";

    private final Index targetIndex;
    private final Phone phoneNumber;

    /**
     * Creates a {@code DeleteClientCommand} to delete the client at the given displayed index.
     *
     * @param targetIndex index of the client to delete
     */
    public DeleteClientCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.phoneNumber = null;
    }

    //@@author chunghean
    /**
     * Creates a {@code DeleteClientCommand} to delete the client with the given phone number.
     *
     * @param phoneNumber phone number of the client to delete
     */
    public DeleteClientCommand(Phone phoneNumber) {
        this.phoneNumber = requireNonNull(phoneNumber);
        this.targetIndex = null;
    }

    @Override
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return targetIndex == null
                ? Optional.empty()
                : Optional.of(CommandResult.DisplayType.CLIENTS);
    }
    //@@author

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Client clientToDelete;
        if (targetIndex != null) {
            List<Client> lastShownList = model.getFilteredClientList();

            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
            }

            clientToDelete = lastShownList.get(targetIndex.getZeroBased());
        } else {
            clientToDelete = model.getBubuBook().getClientList().stream()
                    .filter(client -> client.getPhone().equals(phoneNumber))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(MESSAGE_CLIENT_NOT_FOUND));
            //@@author
        }

        long petCount = model.getBubuBook().getPetList().stream()
                .filter(pet -> pet.getOwner().isSameClient(clientToDelete))
                .count();
        long appointmentCount = model.getBubuBook().getAppointmentList().stream()
                .filter(appointment -> appointment.isForOwner(clientToDelete))
                //@@author
                .count();

        model.deleteClient(clientToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete), formatCount(petCount, "pet"),
                formatCount(appointmentCount, "appointment")));
    }

    /** Formats a count using the singular form when exactly one record was removed. */
    private static String formatCount(long count, String singularNoun) {
        return count + " " + singularNoun + (count == 1 ? "" : "s");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteClientCommand otherDeleteClientCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherDeleteClientCommand.targetIndex)
                && Objects.equals(phoneNumber, otherDeleteClientCommand.phoneNumber);
    }

    @Override
    public String toString() {
        if (targetIndex != null) {
            return new ToStringBuilder(this)
                    .add("targetIndex", targetIndex)
                    .toString();
        }
        return new ToStringBuilder(this)
                .add("phoneNumber", phoneNumber)
                .toString();
    }
}
