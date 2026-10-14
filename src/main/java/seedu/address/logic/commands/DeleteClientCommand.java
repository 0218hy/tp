package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;

/**
 * Deletes a person identified using its displayed index or phone number from the address book.
 */
public class DeleteClientCommand extends Command {

    public static final String COMMAND_WORD = "delete-client";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes a person by displayed index or phone number.\n"
            + "Parameters: INDEX (must be a positive integer), or i/PHONE_NUMBER\n"
            + "Examples: " + COMMAND_WORD + " 1\n"
            + "          " + COMMAND_WORD + " i/91234567";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";

    private final Index targetIndex;
    private final Phone phoneNumber;

    /**
     * Creates a {@code DeleteClientCommand} to delete the person at the given displayed index.
     *
     * @param targetIndex index of the person to delete
     */
    public DeleteClientCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.phoneNumber = null;
    }

    /**
     * Creates a {@code DeleteClientCommand} to delete the person with the given phone number.
     *
     * @param phoneNumber phone number of the person to delete
     */
    public DeleteClientCommand(Phone phoneNumber) {
        this.phoneNumber = requireNonNull(phoneNumber);
        this.targetIndex = null;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToDelete;
        if (targetIndex != null) {
            List<Person> lastShownList = model.getFilteredPersonList();

            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }

            personToDelete = lastShownList.get(targetIndex.getZeroBased());
        } else {
            //TODO: use SLAP
            personToDelete = model.getAddressBook().getPersonList().stream()
                    .filter(person -> person.getPhone().equals(phoneNumber))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(
                            "No person with the given phone number exists."));
        }

        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
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
