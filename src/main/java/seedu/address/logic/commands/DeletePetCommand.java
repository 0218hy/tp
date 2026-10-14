package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Deletes a pet identified by its name and its owner's phone number from the address book.
 */
public class DeletePetCommand extends Command {

    public static final String COMMAND_WORD = "delete-pet";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the pet identified by its name and its owner's phone number.\n"
            + "Parameters: p/PET_NAME i/OWNER_IDENTIFIER\n"
            + "Example: " + COMMAND_WORD + " p/Milo i/98765432";

    public static final String MESSAGE_DELETE_PET_SUCCESS = "Deleted pet: %1$s";
    public static final String MESSAGE_PET_NOT_FOUND =
            "No pet with the specified name and owner phone number was found.";

    private final PetName petName;
    private final Phone ownerPhone;

    /**
     * Creates a {@code DeletePetCommand} to delete the pet identified by the given name and owner phone number.
     */
    public DeletePetCommand(PetName petName, Phone ownerPhone) {
        this.petName = requireNonNull(petName);
        this.ownerPhone = requireNonNull(ownerPhone);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Pet petToDelete = model.getAddressBook().getPetList().stream()
                .filter(pet -> pet.getName().equals(petName) && pet.getOwner().getPhone().equals(ownerPhone))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_PET_NOT_FOUND));

        model.deletePet(petToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PET_SUCCESS, petToDelete.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DeletePetCommand otherDeletePetCommand)) {
            return false;
        }

        return petName.equals(otherDeletePetCommand.petName)
                && ownerPhone.equals(otherDeletePetCommand.ownerPhone);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("petName", petName)
                .add("ownerPhone", ownerPhone)
                .toString();
    }
}
