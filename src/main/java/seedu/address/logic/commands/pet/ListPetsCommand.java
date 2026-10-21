package seedu.address.logic.commands.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;

import java.util.List;
import java.util.Objects;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;

/**
 * Lists saved pet profiles, optionally restricting the result to one owner.
 * Selects the pet view without changing saved records or the client list's filter.
 */
public class ListPetsCommand extends Command {
    public static final String COMMAND_WORD = "list-pets";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " [" + PREFIX_OWNER_IDENTIFIER
            + "OWNER_IDENTIFIER]";
    public static final String MESSAGE_ALL_PETS = "All pet profiles";
    public static final String MESSAGE_NO_PETS = "No saved pet profiles.";
    public static final String MESSAGE_OWNER_NOT_FOUND = "No saved client has this phone number.";
    public static final String MESSAGE_OWNER_PETS = "Pets under %s";
    public static final String MESSAGE_OWNER_NO_PETS = "%s has no saved pet profiles.";
    private static final String DETAIL_SEPARATOR = " ";
    private static final String LINE_SEPARATOR = "\n";
    private static final String GROUP_SEPARATOR = "\n\n";

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
     * Filters and formats the saved pet profiles in owner and pet name order.
     *
     * @param model The model containing the saved owners and pet profiles.
     * @return The formatted profiles, or a message when no pets match.
     * @throws CommandException if the specified owner phone is not registered.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (predicate == null) {
            model.updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
            return new CommandResult(formatAllPets(model.getFilteredPetList()), CommandResult.DisplayType.PETS);
        }

        Client owner = findOwner(model);
        model.updateFilteredPetList(predicate);
        List<Pet> pets = model.getFilteredPetList();
        return new CommandResult(formatOwnerPets(owner, pets), CommandResult.DisplayType.PETS);
    }

    /**
     * Returns the registered owner with the requested phone number.
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

    /**
     * Formats profiles that the model has already ordered by owner and pet name.
     * Consecutive pets belonging to the same owner share a heading.
     *
     * @param savedPets The profiles in the model's display order; this method does not sort them.
     */
    private String formatAllPets(List<Pet> savedPets) {
        if (savedPets.isEmpty()) {
            return MESSAGE_NO_PETS;
        }

        StringBuilder result = new StringBuilder(MESSAGE_ALL_PETS);
        Client previousOwner = null;
        for (Pet pet : savedPets) {
            if (previousOwner == null || !pet.isOwnedBy(previousOwner.getPhone())) {
                previousOwner = pet.getOwner();
                result.append(GROUP_SEPARATOR).append(previousOwner.getName());
            }
            result.append(LINE_SEPARATOR).append(formatPet(pet));
        }
        return result.toString();
    }

    /**
     * Formats the selected owner's profiles, or their empty result.
     *
     * @param owner The registered owner whose name appears in the result.
     * @param pets The selected owner's profiles in display order.
     */
    private String formatOwnerPets(Client owner, List<Pet> pets) {
        if (pets.isEmpty()) {
            return String.format(MESSAGE_OWNER_NO_PETS, owner.getName());
        }

        StringBuilder result = new StringBuilder(String.format(MESSAGE_OWNER_PETS, owner.getName()));
        for (Pet pet : pets) {
            assert pet.isOwnedBy(owner.getPhone())
                    : "An owner-filtered listing must contain only that owner's pets";
            result.append(LINE_SEPARATOR).append(formatPet(pet));
        }
        return result.toString();
    }

    /**
     * Formats one pet's name, species, optional breed, and care requirements on a single line.
     */
    private String formatPet(Pet pet) {
        StringBuilder details = new StringBuilder(pet.getName().toString());
        appendDetail(details, pet.getSpecies().getDisplayName());
        if (pet.getBreed() != null) {
            appendDetail(details, pet.getBreed().toString());
        }
        appendDetail(details, pet.getRequirement().toString());
        return details.toString();
    }

    /**
     * Appends a displayed field with a space separating it from the previous field.
     */
    private void appendDetail(StringBuilder details, String value) {
        details.append(DETAIL_SEPARATOR).append(value);
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
