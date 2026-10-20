package seedu.address.logic.commands.pet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;
import seedu.address.testutil.PetBuilder;

/** Contains integration and unit tests for {@code EditPetCommand}. */
public class EditPetCommandTest {
    private final Pet milo = new PetBuilder(ALICE).withBreed("Golden retriever").build();
    private final Pet mochi = new PetBuilder(ALICE).withName("Mochi").build();
    private final Pet miloWithDifferentOwner = new PetBuilder(BOB).build();
    private Model model;

    @BeforeEach
    public void setUp() {
        BubuBook bubuBook = new BubuBook();
        bubuBook.addClient(ALICE);
        bubuBook.addClient(BOB);
        bubuBook.addPet(milo);
        bubuBook.addPet(mochi);
        bubuBook.addPet(miloWithDifferentOwner);
        model = new ModelManager(bubuBook, new UserPrefs());
    }

    @Test
    public void execute_validChanges_updatesPetAndPreservesOwner() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setName(new PetName("Milo Bean"));
        descriptor.setSpecies(Species.CAT);
        descriptor.setBreed(new Breed("British Shorthair"));
        descriptor.setRequirement(new Requirement("Prefers quiet drying"));
        EditPetCommand command = new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor);

        Pet editedPet = new PetBuilder(milo).withName("Milo Bean").withSpecies(Species.CAT)
                .withBreed("British Shorthair").withRequirement("Prefers quiet drying").build();
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.setPet(milo, editedPet);

        assertCommandSuccess(command, model, String.format(EditPetCommand.MESSAGE_SUCCESS, editedPet.getName()),
                expectedModel);
        assertTrue(model.hasPet(editedPet));
        assertTrue(model.getBubuBook().getPetList().stream()
                .anyMatch(pet -> pet.getName().equals(editedPet.getName()) && pet.getOwner().equals(ALICE)));
    }

    @Test
    public void execute_emptyBreed_clearsBreed() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setBreed(null);
        EditPetCommand command = new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor);
        Pet editedPet = new Pet(milo.getName(), ALICE, milo.getSpecies(), milo.getRequirement());
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.setPet(milo, editedPet);

        assertCommandSuccess(command, model, String.format(EditPetCommand.MESSAGE_SUCCESS, editedPet.getName()),
                expectedModel);
        assertTrue(model.getBubuBook().getPetList().stream()
                .filter(pet -> pet.getName().equals(milo.getName()) && pet.getOwner().equals(ALICE))
                .allMatch(pet -> pet.getBreed() == null));
    }

    @Test
    public void execute_duplicateUpdatedName_throwsCommandException() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setName(new PetName("  mOcHi  "));

        assertCommandFailure(new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor), model,
                String.format(EditPetCommand.MESSAGE_DUPLICATE_PET, mochi.getName()));
    }

    @Test
    public void execute_petDoesNotExist_throwsCommandException() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));

        assertCommandFailure(new EditPetCommand(new PetName("Luna"), ALICE.getPhone(), descriptor), model,
                EditPetCommand.MESSAGE_PET_NOT_FOUND);
        assertCommandFailure(new EditPetCommand(milo.getName(), new Phone("81111112"), descriptor), model,
                EditPetCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_nameWithDifferentCaseAndWhitespace_updatesMatchingPet() {
        Pet petWithRepeatedWhitespace = new PetBuilder(ALICE).withName("Milo  Bean").build();
        model.addPet(petWithRepeatedWhitespace);
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));
        Pet editedPet = new PetBuilder(petWithRepeatedWhitespace).withRequirement("Needs a short walk").build();
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.setPet(petWithRepeatedWhitespace, editedPet);

        assertCommandSuccess(new EditPetCommand(new PetName("mIlO bEaN"), ALICE.getPhone(), descriptor), model,
                String.format(EditPetCommand.MESSAGE_SUCCESS, editedPet.getName()), expectedModel);
    }

    @Test
    public void execute_normalizedName_updatesOnlyPetForSpecifiedOwner() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));
        Pet editedPet = new PetBuilder(milo).withRequirement("Needs a short walk").build();
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.setPet(milo, editedPet);

        assertCommandSuccess(new EditPetCommand(new PetName("mIlO"), ALICE.getPhone(), descriptor), model,
                String.format(EditPetCommand.MESSAGE_SUCCESS, editedPet.getName()), expectedModel);
        assertTrue(model.hasPet(miloWithDifferentOwner));
    }

    @Test
    public void execute_petHiddenByFilter_updatesPetAndResetsFilter() {
        model.updateFilteredPetList(pet -> false);
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));
        Pet editedPet = new PetBuilder(milo).withRequirement("Needs a short walk").build();
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.updateFilteredPetList(pet -> false);
        expectedModel.setPet(milo, editedPet);
        expectedModel.updateFilteredPetList(Model.PREDICATE_SHOW_ALL_PETS);

        assertCommandSuccess(new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor), model,
                String.format(EditPetCommand.MESSAGE_SUCCESS, editedPet.getName()), expectedModel);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));

        assertThrows(NullPointerException.class, () ->
                new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor).execute(null));
    }

    @Test
    public void constructor_descriptorIsDefensivelyCopied() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));
        EditPetCommand command = new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor);
        descriptor.setName(new PetName("Milo Changed"));

        Pet expectedPet = new PetBuilder(milo).withRequirement("Needs a short walk").build();
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.setPet(milo, expectedPet);

        assertCommandSuccess(command, model, String.format(EditPetCommand.MESSAGE_SUCCESS, expectedPet.getName()),
                expectedModel);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        assertThrows(NullPointerException.class, () -> new EditPetCommand(null, ALICE.getPhone(), descriptor));
        assertThrows(NullPointerException.class, () -> new EditPetCommand(milo.getName(), null, descriptor));
        assertThrows(NullPointerException.class, () -> new EditPetCommand(milo.getName(), ALICE.getPhone(), null));
    }

    @Test
    public void equals() {
        EditPetCommand.EditPetDescriptor descriptor = new EditPetCommand.EditPetDescriptor();
        descriptor.setSpecies(Species.CAT);
        EditPetCommand editMilo = new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor);
        EditPetCommand editMiloForBob = new EditPetCommand(milo.getName(), BOB.getPhone(), descriptor);

        assertTrue(editMilo.equals(editMilo));
        assertTrue(editMilo.equals(new EditPetCommand(milo.getName(), ALICE.getPhone(), descriptor)));
        assertFalse(editMilo.equals(editMiloForBob));
        assertFalse(editMilo.equals(new DeleteClientCommand(
                seedu.address.commons.core.index.Index.fromOneBased(1))));
        assertFalse(editMilo.equals(null));
    }
}
