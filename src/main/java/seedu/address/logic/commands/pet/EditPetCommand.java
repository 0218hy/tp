package seedu.address.logic.commands.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_NEW_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_SPECIES;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/**
 * Edits a pet profile identified by its current name and its owner's phone number.
 */
public class EditPetCommand extends Command {
    public static final String COMMAND_WORD = "edit-pet";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits a pet profile identified by its current name and owner's phone number.\n"
            + "Parameters: " + PREFIX_PET_NAME + "PET_NAME " + PREFIX_OWNER_IDENTIFIER + "OWNER_IDENTIFIER "
            + "[" + PREFIX_NEW_PET_NAME + "NEW_PET_NAME] "
            + "[" + PREFIX_SPECIES + "SPECIES] "
            + "[" + PREFIX_BREED + "BREED] "
            + "[" + PREFIX_REQUIREMENTS + "REQUIREMENTS]\n"
            + "Example: " + COMMAND_WORD + " p/Mochi i/87438807 n/Mochi Bean r/Prefers quiet drying";
    public static final String MESSAGE_SUCCESS = "Edited pet: %s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_PET_NOT_FOUND =
            "No pet with the specified name and owner phone number was found.";
    public static final String MESSAGE_DUPLICATE_PET = "This client already has a pet named %s.";

    private final PetName petName;
    private final Phone ownerPhone;
    private final EditPetDescriptor editPetDescriptor;

    /**
     * Creates an {@code EditPetCommand} for a pet identified by its current name and owner's phone number.
     */
    public EditPetCommand(PetName petName, Phone ownerPhone, EditPetDescriptor editPetDescriptor) {
        this.petName = requireNonNull(petName);
        this.ownerPhone = requireNonNull(ownerPhone);
        this.editPetDescriptor = new EditPetDescriptor(requireNonNull(editPetDescriptor));
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Pet petToEdit = findPet(model);
        Pet editedPet = createEditedPet(petToEdit, editPetDescriptor);

        checkForDuplicate(model, petToEdit, editedPet);
        model.setPet(petToEdit, editedPet);
        model.updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, editedPet.getName()));
    }

    private Pet findPet(Model model) throws CommandException {
        return model.getBubuBook().getPetList().stream()
                .filter(pet -> pet.getName().equals(petName) && pet.getOwner().getPhone().equals(ownerPhone))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_PET_NOT_FOUND));
    }

    private void checkForDuplicate(Model model, Pet petToEdit, Pet editedPet) throws CommandException {
        for (Pet existingPet : model.getBubuBook().getPetList()) {
            if (!existingPet.equals(petToEdit) && existingPet.isSamePet(editedPet)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_PET, existingPet.getName()));
            }
        }
    }

    /**
     * Creates an edited copy of {@code petToEdit}, preserving its owner.
     */
    private static Pet createEditedPet(Pet petToEdit, EditPetDescriptor editPetDescriptor) {
        PetName updatedName = editPetDescriptor.getName().orElse(petToEdit.getName());
        Species updatedSpecies = editPetDescriptor.getSpecies().orElse(petToEdit.getSpecies());
        Breed updatedBreed = editPetDescriptor.isBreedEdited()
                ? editPetDescriptor.getBreed() : petToEdit.getBreed();
        Requirement updatedRequirement = editPetDescriptor.getRequirement().orElse(petToEdit.getRequirement());

        return new Pet(updatedName, petToEdit.getOwner(), updatedSpecies, updatedBreed, updatedRequirement);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EditPetCommand otherCommand)) {
            return false;
        }
        return petName.equals(otherCommand.petName)
                && ownerPhone.equals(otherCommand.ownerPhone)
                && editPetDescriptor.equals(otherCommand.editPetDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("petName", petName)
                .add("ownerPhone", ownerPhone)
                .add("editPetDescriptor", editPetDescriptor)
                .toString();
    }

    /** Stores the optional changes to a pet profile. */
    public static class EditPetDescriptor {
        private PetName name;
        private Species species;
        private Breed breed;
        private boolean isBreedEdited;
        private Requirement requirement;

        public EditPetDescriptor() {}

        /** Creates a defensive copy of the supplied descriptor. */
        public EditPetDescriptor(EditPetDescriptor toCopy) {
            setName(toCopy.name);
            setSpecies(toCopy.species);
            if (toCopy.isBreedEdited) {
                setBreed(toCopy.breed);
            }
            setRequirement(toCopy.requirement);
        }

        /** Returns whether at least one field has been supplied for editing. */
        public boolean isAnyFieldEdited() {
            return name != null || species != null || isBreedEdited || requirement != null;
        }

        public void setName(PetName name) {
            this.name = name;
        }

        public Optional<PetName> getName() {
            return Optional.ofNullable(name);
        }

        public void setSpecies(Species species) {
            this.species = species;
        }

        public Optional<Species> getSpecies() {
            return Optional.ofNullable(species);
        }

        /**
         * Sets the pet's breed. A {@code null} value explicitly clears an existing breed.
         */
        public void setBreed(Breed breed) {
            this.breed = breed;
            isBreedEdited = true;
        }

        public Breed getBreed() {
            return breed;
        }

        public boolean isBreedEdited() {
            return isBreedEdited;
        }

        public void setRequirement(Requirement requirement) {
            this.requirement = requirement;
        }

        public Optional<Requirement> getRequirement() {
            return Optional.ofNullable(requirement);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof EditPetDescriptor otherDescriptor)) {
                return false;
            }
            return Objects.equals(name, otherDescriptor.name)
                    && species == otherDescriptor.species
                    && Objects.equals(breed, otherDescriptor.breed)
                    && isBreedEdited == otherDescriptor.isBreedEdited
                    && Objects.equals(requirement, otherDescriptor.requirement);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, species, breed, isBreedEdited, requirement);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("species", species)
                    .add("breed", breed)
                    .add("isBreedEdited", isBreedEdited)
                    .add("requirement", requirement)
                    .toString();
        }
    }
}
