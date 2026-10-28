package seedu.address.logic.commands.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_NEW_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_SPECIES;

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

    //@@author khangsian0412
    /**
     * Creates an {@code EditPetCommand} for a pet identified by its current name and owner's phone number.
     *
     * @param petName the current pet name used with {@code ownerPhone} to identify the pet
     * @param ownerPhone the phone number identifying the pet's owner
     * @param editPetDescriptor the requested optional changes
     */
    public EditPetCommand(PetName petName, Phone ownerPhone, EditPetDescriptor editPetDescriptor) {
        this.petName = requireNonNull(petName);
        this.ownerPhone = requireNonNull(ownerPhone);
        this.editPetDescriptor = new EditPetDescriptor(requireNonNull(editPetDescriptor));
    }
    //@@author

    /**
     * Finds the identified pet, applies the requested changes, and replaces its stored profile.
     * The owner and active pet filter are preserved after a successful edit.
     *
     * @param model the model containing the pet profiles
     * @return the result describing the edited pet
     * @throws CommandException if no pet matches the supplied name and owner, or the edit creates a duplicate pet
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Pet petToEdit = findPet(model);
        Pet editedPet = createEditedPet(petToEdit, editPetDescriptor);

        checkForDuplicate(model, petToEdit, editedPet);
        model.setPet(petToEdit, editedPet);
        return new CommandResult(String.format(MESSAGE_SUCCESS, editedPet.getName()));
    }

    //@@author khangsian0412
    /**
     * Returns the pet identified by the normalized name and owner phone number stored in this command.
     *
     * @param model the model whose saved pets are searched
     * @return the matching pet
     * @throws CommandException if no saved pet has both the specified normalized name and owner phone number
     */
    private Pet findPet(Model model) throws CommandException {
        return model.getBubuBook().getPetList().stream()
                .filter(pet -> pet.getName().getNormalizedName().equals(petName.getNormalizedName())
                        && pet.getOwner().getPhone().equals(ownerPhone))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_PET_NOT_FOUND));
    }
    //@@author

    /**
     * Rejects an edit that gives the pet the same normalized name as another pet belonging to the same owner.
     *
     * @param model the model whose saved pets are checked
     * @param petToEdit the original pet, which is excluded from the duplicate check
     * @param editedPet the proposed replacement pet
     * @throws CommandException if another pet owned by the same client already has the edited name
     */
    private void checkForDuplicate(Model model, Pet petToEdit, Pet editedPet) throws CommandException {
        for (Pet existingPet : model.getBubuBook().getPetList()) {
            if (!existingPet.equals(petToEdit) && existingPet.isSamePet(editedPet)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_PET, existingPet.getName()));
            }
        }
    }

    /**
     * Creates an edited copy of {@code petToEdit}, preserving its owner.
     *
     * @param petToEdit the existing pet whose unspecified fields are retained
     * @param editPetDescriptor the optional replacement field values
     * @return a pet with the requested changes and the original owner
     */
    private static Pet createEditedPet(Pet petToEdit, EditPetDescriptor editPetDescriptor) {
        PetName updatedName = editPetDescriptor.getName().orElse(petToEdit.getName());
        Species updatedSpecies = editPetDescriptor.getSpecies().orElse(petToEdit.getSpecies());
        Breed updatedBreed = editPetDescriptor.isBreedEdited()
                ? editPetDescriptor.getBreed() : petToEdit.getBreed();
        Requirement updatedRequirement = editPetDescriptor.getRequirement().orElse(petToEdit.getRequirement());

        return new Pet(updatedName, petToEdit.getOwner(), updatedSpecies, updatedBreed, updatedRequirement);
    }

    /**
     * Returns whether this command identifies the same pet and requests the same edits as {@code other}.
     *
     * @param other the object to compare with this command
     * @return {@code true} if both commands have equal identifiers and edit descriptors
     */
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

    /**
     * Returns a string representation of this command and its requested edits.
     *
     * @return a string representation of this command
     */
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

        /** Creates an empty descriptor with no requested changes. */
        public EditPetDescriptor() {}

        /**
         * Creates a defensive copy of the supplied descriptor, including whether its breed was explicitly edited.
         *
         * @param toCopy the descriptor to copy
         */
        public EditPetDescriptor(EditPetDescriptor toCopy) {
            setName(toCopy.name);
            setSpecies(toCopy.species);
            if (toCopy.isBreedEdited) {
                setBreed(toCopy.breed);
            }
            setRequirement(toCopy.requirement);
        }

        //@@author khangsian0412
        /**
         * Returns whether at least one field has been supplied for editing.
         *
         * @return {@code true} if this descriptor contains a replacement value or an explicit breed removal
         */
        public boolean isAnyFieldEdited() {
            return name != null || species != null || isBreedEdited || requirement != null;
        }
        //@@author

        /**
         * Sets the replacement pet name.
         *
         * @param name the replacement name, or {@code null} when no name change is requested
         */
        public void setName(PetName name) {
            this.name = name;
        }

        /**
         * Returns the replacement pet name when one was supplied.
         *
         * @return an optional replacement name
         */
        public Optional<PetName> getName() {
            return Optional.ofNullable(name);
        }

        /**
         * Sets the replacement species.
         *
         * @param species the replacement species, or {@code null} when no species change is requested
         */
        public void setSpecies(Species species) {
            this.species = species;
        }

        /**
         * Returns the replacement species when one was supplied.
         *
         * @return an optional replacement species
         */
        public Optional<Species> getSpecies() {
            return Optional.ofNullable(species);
        }

        //@@author khangsian0412
        /**
         * Sets the pet's breed. A {@code null} value explicitly clears an existing breed.
         *
         * @param breed the replacement breed, or {@code null} to remove the existing breed
         */
        public void setBreed(Breed breed) {
            this.breed = breed;
            isBreedEdited = true;
        }

        /**
         * Returns the replacement breed, or {@code null} when the breed is removed or unchanged.
         * Use {@link #isBreedEdited()} to distinguish those cases.
         *
         * @return the replacement breed, or {@code null}
         */
        public Breed getBreed() {
            return breed;
        }

        /**
         * Returns whether the breed has been explicitly changed, including when it is cleared.
         *
         * @return {@code true} if a replacement breed or explicit breed removal was supplied
         */
        public boolean isBreedEdited() {
            return isBreedEdited;
        }
        //@@author

        /**
         * Sets the replacement grooming requirement.
         *
         * @param requirement the replacement requirement, or {@code null} when no requirement change is requested
         */
        public void setRequirement(Requirement requirement) {
            this.requirement = requirement;
        }

        /**
         * Returns the replacement grooming requirement when one was supplied.
         *
         * @return an optional replacement requirement
         */
        public Optional<Requirement> getRequirement() {
            return Optional.ofNullable(requirement);
        }

        /**
         * Returns whether this descriptor contains the same requested field changes as {@code other}.
         *
         * @param other the object to compare with this descriptor
         * @return {@code true} if both descriptors contain equal edits
         */
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

        /**
         * Returns a hash code derived from the requested field changes.
         *
         * @return this descriptor's hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(name, species, breed, isBreedEdited, requirement);
        }

        /**
         * Returns a string representation of the requested field changes.
         *
         * @return a string representation of this descriptor
         */
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
