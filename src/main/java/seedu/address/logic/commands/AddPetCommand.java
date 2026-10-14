package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_SPECIES;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/**
 * Adds a pet profile to a registered client identified by phone number.
 * Pets with the same normalized name cannot belong to the same client.
 */
public class AddPetCommand extends Command {
    public static final String COMMAND_WORD = "add-pet";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + PREFIX_PET_NAME + "PET_NAME "
            + PREFIX_OWNER_IDENTIFIER + "OWNER_IDENTIFIER " + PREFIX_SPECIES + "SPECIES ["
            + PREFIX_BREED + "BREED] " + PREFIX_REQUIREMENTS + "REQUIREMENTS";
    public static final String MESSAGE_OWNER_NOT_FOUND =
            "The owner identifier does not belong to the specified client.";
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
     * @throws CommandException if the owner is not registered or already has a pet with this name.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person owner = findOwner(model);
        Pet pet = new Pet(name, owner, species, breed, requirement);
        checkForDuplicate(model, pet);
        model.addPet(pet);
        return new CommandResult(formatSuccessMessage(owner));
    }

    /**
     * Finds the registered client with the supplied phone number.
     */
    private Person findOwner(Model model) throws CommandException {
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getPhone().value.equals(ownerPhone))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_OWNER_NOT_FOUND));
    }

    /**
     * Rejects a pet if its owner already has one with the same normalized name.
     */
    private void checkForDuplicate(Model model, Pet pet) throws CommandException {
        for (Pet existingPet : model.getAddressBook().getPetList()) {
            if (existingPet.isSamePet(pet)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_PET, existingPet.getName()));
            }
        }
    }

    /**
     * Formats the success message, including the breed when it is known.
     */
    private String formatSuccessMessage(Person owner) {
        return breed == null
                ? String.format(MESSAGE_SUCCESS, name, formatSpecies(), owner.getName())
                : String.format(MESSAGE_SUCCESS_WITH_BREED, name, formatSpecies(), breed, owner.getName());
    }

    /**
     * Returns the species label used in the success message.
     */
    private String formatSpecies() {
        return switch (species) {
            case DOG -> "Dog";
            case CAT -> "Cat";
            case RABBIT -> "Rabbit";
            case OTHER -> "Other";
            case GUINEA_PIG -> "Guinea pig";
        };
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
