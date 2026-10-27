package seedu.address.logic.commands;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    //@@author chunghean
    /**
     * Returns the display type required before this command can resolve an index.
     * Commands that do not use an index are independent of the current displayed list.
     */
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return Optional.empty();
    }
    //@@author

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
