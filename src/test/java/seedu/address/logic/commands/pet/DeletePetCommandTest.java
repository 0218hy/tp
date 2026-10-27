package seedu.address.logic.commands.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Phone;
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
        BubuBook bubuBook = new BubuBook();
        bubuBook.addClient(ALICE);
        bubuBook.addClient(BOB);
        bubuBook.addPet(milo);
        bubuBook.addPet(miloWithDifferentOwner);
        model = new ModelManager(bubuBook, new UserPrefs());
    }

    @Test
    public void execute_matchingNameAndOwnerPhone_deletesPet() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deletePet(milo);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, milo.getName()), expectedModel);
    }

    @Test
    public void execute_validIndex_deletesPetFromFilteredList() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(Index.fromOneBased(1));
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deletePet(milo);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, milo.getName()), expectedModel);
    }

    //@@author khangsian0412
    @Test
    public void execute_lastValidIndex_deletesLastDisplayedPet() {
        // BVA: the list-size index is the last valid index (n), while n + 1 is invalid.
        DeletePetCommand deletePetCommand = new DeletePetCommand(Index.fromOneBased(2));
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deletePet(miloWithDifferentOwner);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, miloWithDifferentOwner.getName()),
                expectedModel);
    }
    //@@author

    //@@author khangsian0412
    @Test
    public void execute_indexTargetsFirstPetInFilteredList_notFirstPetInAllPets() {
        // EP: index deletion uses the visible filtered list, not the full pet list.
        model.updateFilteredPetList(pet -> pet.getOwner().equals(BOB));
        DeletePetCommand deletePetCommand = new DeletePetCommand(Index.fromOneBased(1));
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.updateFilteredPetList(pet -> pet.getOwner().equals(BOB));
        expectedModel.deletePet(miloWithDifferentOwner);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, miloWithDifferentOwner.getName()),
                expectedModel);
        assertTrue(model.hasPet(milo));
        assertFalse(model.hasPet(miloWithDifferentOwner));
    }
    //@@author

    //@@author khangsian0412
    @Test
    public void execute_matchingNameWithDifferentOwner_deletesOnlyMatchingPet() throws CommandException {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());

        deletePetCommand.execute(model);

        assertFalse(model.hasPet(milo));
        assertTrue(model.hasPet(miloWithDifferentOwner));
    }
    //@@author

    //@@author khangsian0412
    @Test
    public void execute_petDoesNotExist_throwsCommandException() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(new PetName("Luna"), ALICE.getPhone());

        assertCommandFailure(deletePetCommand, model, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }
    //@@author

    //@@author khangsian0412
    @Test
    public void execute_matchingNameWithWrongOwnerPhone_throwsCommandException() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), new Phone("81111112"));

        assertCommandFailure(deletePetCommand, model, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }
    //@@author

    //@@author khangsian0412
    @Test
    public void execute_emptyPetList_throwsCommandException() {
        Model emptyModel = new ModelManager();
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());

        assertCommandFailure(deletePetCommand, emptyModel, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }
    //@@author

    //@@author chunghean
    @Test
    public void execute_invalidIndex_throwsCommandException() {
        assertCommandFailure(new DeletePetCommand(Index.fromOneBased(3)), model,
                DeletePetCommand.MESSAGE_INVALID_PET_DISPLAYED_INDEX);
    }
    //@@author

    @Test
    public void execute_petHiddenByFilter_deletesPet() {
        model.updateFilteredPetList(pet -> false);
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.updateFilteredPetList(pet -> false);
        expectedModel.deletePet(milo);

        assertCommandSuccess(deletePetCommand, model,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, milo.getName()), expectedModel);
    }

    //@@author khangsian0412
    @Test
    public void execute_deletedPetAgain_throwsCommandException() throws CommandException {
        DeletePetCommand deletePetCommand = new DeletePetCommand(milo.getName(), ALICE.getPhone());

        deletePetCommand.execute(model);

        assertCommandFailure(deletePetCommand, model, DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }
    //@@author

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeletePetCommand((Index) null));
        assertThrows(NullPointerException.class, () -> new DeletePetCommand(null, ALICE.getPhone()));
        assertThrows(NullPointerException.class, () -> new DeletePetCommand(milo.getName(), null));
    }

    //@@author chunghean
    @Test
    public void getRequiredDisplayType_dependsOnTarget() {
        assertEquals(java.util.Optional.of(CommandResult.DisplayType.PETS),
                new DeletePetCommand(Index.fromOneBased(1)).getRequiredDisplayType());
        assertEquals(java.util.Optional.empty(),
                new DeletePetCommand(milo.getName(), ALICE.getPhone()).getRequiredDisplayType());
    }
    //@@author

    @Test
    public void equals() {
        DeletePetCommand deleteMiloForAlice = new DeletePetCommand(milo.getName(), ALICE.getPhone());
        DeletePetCommand deleteMiloForBob = new DeletePetCommand(milo.getName(), BOB.getPhone());

        assertTrue(deleteMiloForAlice.equals(deleteMiloForAlice));
        assertTrue(deleteMiloForAlice.equals(new DeletePetCommand(milo.getName(), ALICE.getPhone())));
        assertFalse(deleteMiloForAlice.equals(deleteMiloForBob));
        assertFalse(deleteMiloForAlice.equals(new DeletePetCommand(Index.fromOneBased(1))));
        assertFalse(deleteMiloForAlice.equals(new DeletePetCommand(new PetName("Luna"), ALICE.getPhone())));
        assertFalse(deleteMiloForAlice.equals(null));
        assertFalse(deleteMiloForAlice.equals(
                new DeleteClientCommand(seedu.address.commons.core.index.Index.fromOneBased(1))));
    }

    @Test
    public void toStringMethod() {
        DeletePetCommand deletePetCommand = new DeletePetCommand(new PetName("Milo"), new Phone("98765432"));
        Index index = Index.fromOneBased(1);

        assertEquals(DeletePetCommand.class.getCanonicalName() + "{petName=Milo, ownerPhone=98765432}",
                deletePetCommand.toString());
        assertEquals(DeletePetCommand.class.getCanonicalName() + "{targetIndex=" + index + "}",
                new DeletePetCommand(index).toString());
    }
}
