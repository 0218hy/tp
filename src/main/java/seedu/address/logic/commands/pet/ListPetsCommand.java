package seedu.address.logic.commands.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;

import java.util.Objects;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;

// @@author xinyu-zxy
/**
 * Lists saved pet profiles, optionally restricting the result to one owner.
 * Selects the pet view without changing saved records or the client list's filter.
 */
public class ListPetsCommand extends Command {
    public static final String COMMAND_WORD = "list-pets";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all pets or only pets belonging to one client. "
            + "Parameters: [" + PREFIX_OWNER_IDENTIFIER + "OWNER_PHONE]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_OWNER_IDENTIFIER + "91234567";
    public static final String MESSAGE_ALL_PETS = "Listed all pets.";
    public static final String MESSAGE_OWNER_NOT_FOUND = "No saved client has this phone number.";
    public static final String MESSAGE_OWNER_PETS = "Found %d pet(s) under %s.";
    public static final String MESSAGE_OWNER_NO_PETS = "%s has no saved pet profiles.";

    private final PetOwnerPhoneMatchesPredicate predicate;

    /**
     * Creates a command for all owners when {@code predicate} is null, or for one owner otherwise.
     *
     * @param predicate The owner filter, or {@code null} to list every owner's pets.
     */
    public ListPetsCommand(PetOwnerPhoneMatchesPredicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Updates the displayed pet profiles and returns concise feedback.
     *
     * @param model The model containing the saved owners and pet profiles.
     * @return Confirmation that all pets are listed, the match count, or an empty-result message.
     * @throws CommandException if the specified owner phone is not registered.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (predicate == null) {
            model.updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
            return new CommandResult(MESSAGE_ALL_PETS, CommandResult.DisplayType.PETS);
        }

        Client owner = findOwner(model);
        model.updateFilteredPetList(predicate);
        int count = model.getFilteredPetList().size();
        String feedback = count == 0 ? String.format(MESSAGE_OWNER_NO_PETS, owner.getName())
                : String.format(MESSAGE_OWNER_PETS, count, owner.getName());
        return new CommandResult(feedback, CommandResult.DisplayType.PETS);
    }

    /**
     * Returns the registered owner with the requested phone number.
     * This method is called only for an owner-filtered listing.
     *
     * @param model The model whose registered owners are searched.
     * @throws CommandException if no registered client has the requested phone number.
     */
    private Client findOwner(Model model) throws CommandException {
        assert predicate != null : "Owner lookup is only needed for an owner-filtered listing";
        for (Client client : model.getBubuBook().getClientList()) {
            if (client.getPhone().equals(predicate.getOwnerPhone())) {
                return client;
            }
        }
        throw new CommandException(MESSAGE_OWNER_NOT_FOUND);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ListPetsCommand otherCommand
                && Objects.equals(predicate, otherCommand.predicate));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(predicate);
    }
}
