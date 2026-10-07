package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.testutil.PetBuilder;

/**
 * Contains integration and unit tests for {@code DeletePetCommand}.
 */
public class DeletePetCommandTest {

    private final Pet milo = new PetBuilder(ALICE).build();
    private final Pet miloWithDifferentOwner = new PetBuilder(BOB).build();
    private Model model;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        addressBook.addPet(milo);
        addressBook.addPet(miloWithDifferentOwner);
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_matchingNameAndOwnerPhone_deletesPet() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePet(milo);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, milo.getName()), expectedModel);
    }

    @Test
    public void execute_matchingNameWithDifferentOwner_deletesOnlyMatchingPet() throws CommandException {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());

        deletePetCommand.execute(model);

        assertFalse(model.hasPet(milo));
        assertTrue(model.hasPet(miloWithDifferentOwner));
    }

    @Test
    public void execute_petDoesNotExist_throwsCommandException() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(new PetName("Luna"), ALICE.getPhone());

        assertCommandFailure(deletePetCommand, model, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_matchingNameWithWrongOwnerPhone_throwsCommandException() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), new Phone("81111112"));

        assertCommandFailure(deletePetCommand, model, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_emptyPetList_throwsCommandException() {
        Model emptyModel = new ModelManager();
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());

        assertCommandFailure(deletePetCommand, emptyModel, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_petHiddenByFilter_deletesPet() {
        model.updateFilteredPetList(pet -> false);
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPetList(pet -> false);
        expectedModel.deletePet(milo);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, milo.getName()), expectedModel);
    }

    @Test
    public void execute_deletedPetAgain_throwsCommandException() throws CommandException {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());

        deletePetCommand.execute(model);

        assertCommandFailure(deletePetCommand, model, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeletePetCommand(null, ALICE.getPhone()));
        assertThrows(NullPointerException.class, () -> new DeletePetCommand(milo.getName(), null));
    }

    @Test
    public void equals() {
        DeletePetCommand deleteMiloForAlice = new DeletePetCommand(milo.getName(), ALICE.getPhone());
        DeletePetCommand deleteMiloForBob = new DeletePetCommand(milo.getName(), BOB.getPhone());

        assertTrue(deleteMiloForAlice.equals(deleteMiloForAlice));
        assertTrue(deleteMiloForAlice.equals(new DeletePetCommand(milo.getName(), ALICE.getPhone())));
        assertFalse(deleteMiloForAlice.equals(deleteMiloForBob));
        assertFalse(deleteMiloForAlice.equals(null));
        assertFalse(deleteMiloForAlice.equals(
                new DeleteCommand(seedu.address.commons.core.index.Index.fromOneBased(1))));
    }

    @Test
    public void toStringMethod() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(new PetName("Milo"), new Phone("98765432"));

        assertEquals(DeletePetCommand.class.getCanonicalName() + "{petName=Milo, ownerPhone=98765432}",
                deletePetCommand.toString());
    }
}
