package seedu.address.logic.commands.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_SPECIES;

import java.util.Objects;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

// @@author xinyu-zxy
/**
 * Adds a pet profile to a registered client identified by phone number.
 * Pets with the same normalized name cannot belong to the same client.
 */
public class AddPetCommand extends Command {
    public static final String COMMAND_WORD = "add-pet";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a pet to a registered client. "
            + "Parameters: " + PREFIX_PET_NAME + "PET_NAME "
            + PREFIX_OWNER_IDENTIFIER + "OWNER_PHONE "
            + PREFIX_SPECIES + "SPECIES ["
            + PREFIX_BREED + "BREED] " + PREFIX_REQUIREMENTS + "REQUIREMENTS\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_PET_NAME + "Mochi "
            + PREFIX_OWNER_IDENTIFIER + "91234567 " + PREFIX_SPECIES + "Dog "
            + PREFIX_REQUIREMENTS + "Calm during grooming";
    public static final String MESSAGE_OWNER_NOT_FOUND =
            "No saved client has this phone number. Add the client before adding a pet.";
    public static final String MESSAGE_DUPLICATE_PET = "This client already has a pet named %s.";
    public static final String MESSAGE_SUCCESS = "Pet added: %s (%s) under %s.";
    public static final String MESSAGE_SUCCESS_WITH_BREED = "Pet added: %s (%s, %s) under %s.";

    private final PetName name;
    private final String ownerPhone;
    private final Species species;
    private final Breed breed;
    private final Requirement requirement;

    /**
     * Creates a command with the pet's details and the owner's phone number.
     * Breed may be {@code null} when the client does not know it.
     * All other arguments must be non-null.
     */
    public AddPetCommand(PetName name, String ownerPhone, Species species, Breed breed, Requirement requirement) {
        this.name = requireNonNull(name);
        this.ownerPhone = requireNonNull(ownerPhone);
        this.species = requireNonNull(species);
        this.breed = breed;
        this.requirement = requireNonNull(requirement);
    }

    /**
     * Finds the registered owner, checks for a duplicate pet, and adds the profile.
     *
     * @param model Non-null model containing the registered clients and saved pets.
     * @return Feedback describing the added pet and its owner.
     * @throws CommandException if the owner is not registered or already has a pet with this name.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Client owner = findOwner(model);
        Pet pet = new Pet(name, owner, species, breed, requirement);
        checkForDuplicate(model, pet);
        model.addPet(pet);
        assert model.hasPet(pet) : "A successfully added pet must be present in the book";
        return new CommandResult(formatSuccessMessage(owner));
    }

    /**
     * Finds the registered client with the supplied phone number.
     * Searches all saved clients, including those hidden by the current client filter.
     *
     * @throws CommandException if no registered client has that phone number.
     */
    private Client findOwner(Model model) throws CommandException {
        for (Client client : model.getBubuBook().getClientList()) {
            if (client.hasPhone(ownerPhone)) {
                return client;
            }
        }
        throw new CommandException(MESSAGE_OWNER_NOT_FOUND);
    }

    /**
     * Rejects a pet if its owner already has one with the same normalized name.
     *
     * @throws CommandException if a duplicate pet exists.
     */
    private void checkForDuplicate(Model model, Pet pet) throws CommandException {
        for (Pet existingPet : model.getBubuBook().getPetList()) {
            if (existingPet.isSamePet(pet)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_PET, existingPet.getName()));
            }
        }
    }

    /**
     * Formats the success message, including the breed only when it is known.
     */
    private String formatSuccessMessage(Client owner) {
        return breed == null
                ? String.format(MESSAGE_SUCCESS, name, species.getDisplayName(), owner.getName())
                : String.format(MESSAGE_SUCCESS_WITH_BREED, name, species.getDisplayName(), breed, owner.getName());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AddPetCommand otherCommand)) {
            return false;
        }
        return name.equals(otherCommand.name) && ownerPhone.equals(otherCommand.ownerPhone)
                && species == otherCommand.species && Objects.equals(breed, otherCommand.breed)
                && requirement.equals(otherCommand.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ownerPhone, species, breed, requirement);
    }
}
