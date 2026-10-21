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

/**
 * Lists saved pet profiles, optionally restricting the result to one owner.
 * Updates the filtered pet list without changing the saved BubuBook or displayed client list.
 */
public class ListPetsCommand extends Command {
    public static final String COMMAND_WORD = "list-pets";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " [" + PREFIX_OWNER_IDENTIFIER
            + "OWNER_IDENTIFIER]";
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
     *
     * @param model The model whose registered owners are searched.
     */
    private Client findOwner(Model model) throws CommandException {
        return model.getBubuBook().getClientList().stream()
                .filter(client -> client.getPhone().equals(predicate.getOwnerPhone()))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_OWNER_NOT_FOUND));
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
