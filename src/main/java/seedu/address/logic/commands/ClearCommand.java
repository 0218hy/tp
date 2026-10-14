package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Clears client and pet records, retaining the appointment schedule.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Clients and pets have been cleared!";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.clearClientsAndPets();
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
