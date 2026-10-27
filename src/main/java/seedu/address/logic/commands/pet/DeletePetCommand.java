package seedu.address.logic.commands.pet;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Deletes a pet identified by its displayed index, or by its name and owner's phone number.
 */
public class DeletePetCommand extends Command {

    public static final String COMMAND_WORD = "delete-pet";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes a pet by displayed index, or by its name and owner's phone number.\n"
            + "Parameters: INDEX (must be a positive integer), or p/PET_NAME i/OWNER_IDENTIFIER\n"
            + "Examples: " + COMMAND_WORD + " 1\n"
            + "          " + COMMAND_WORD + " p/Milo i/98765432";

    public static final String MESSAGE_DELETE_PET_SUCCESS = "Deleted pet: %1$s";
    public static final String MESSAGE_PET_NOT_FOUND =
            "No pet with the specified name and owner phone number was found.";
    public static final String MESSAGE_INVALID_PET_DISPLAYED_INDEX =
            "The pet index provided is invalid.";

    private final Index targetIndex;
    private final PetName petName;
    private final Phone ownerPhone;

    /** Creates a {@code DeletePetCommand} to delete the pet at the given displayed index. */
    public DeletePetCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.petName = null;
        this.ownerPhone = null;
    }

    /**
     * Creates a {@code DeletePetCommand} to delete the pet identified by the given name and owner phone number.
     */
    public DeletePetCommand(PetName petName, Phone ownerPhone) {
        this.targetIndex = null;
        this.petName = requireNonNull(petName);
        this.ownerPhone = requireNonNull(ownerPhone);
    }

    //@@author chunghean
    @Override
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return targetIndex == null
                ? Optional.empty()
                : Optional.of(CommandResult.DisplayType.PETS);
    }
    //@@author

    //@@author khangsian0412
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Pet petToDelete = findPet(model);

        model.deletePet(petToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PET_SUCCESS, petToDelete.getName()));
    }
    //@@author

    private Pet findPet(Model model) throws CommandException {
        if (targetIndex != null) {
            List<Pet> pets = model.getFilteredPetList();
            if (targetIndex.getZeroBased() >= pets.size()) {
                throw new CommandException(MESSAGE_INVALID_PET_DISPLAYED_INDEX);
            }
            return pets.get(targetIndex.getZeroBased());
        }

        return model.getBubuBook().getPetList().stream()
                .filter(pet -> pet.getName().getNormalizedName().equals(petName.getNormalizedName())
                        && pet.getOwner().getPhone().equals(ownerPhone))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_PET_NOT_FOUND));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DeletePetCommand otherDeletePetCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherDeletePetCommand.targetIndex)
                && Objects.equals(petName, otherDeletePetCommand.petName)
                && Objects.equals(ownerPhone, otherDeletePetCommand.ownerPhone);
    }

    @Override
    public String toString() {
        if (targetIndex != null) {
            return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
        }
        return new ToStringBuilder(this)
                .add("petName", petName)
                .add("ownerPhone", ownerPhone)
                .toString();
    }
}
