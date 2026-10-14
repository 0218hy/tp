package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CLIENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientNameContainsKeywordsPredicate;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.client.exceptions.DuplicateClientException;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.GroomingBookBuilder;
import seedu.address.testutil.PetBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new GroomingBook(), new GroomingBook(modelManager.getGroomingBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new GroomingBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasClient_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasClient(null));
    }

    @Test
    public void hasClient_clientNotInGroomingBook_returnsFalse() {
        assertFalse(modelManager.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInGroomingBook_returnsTrue() {
        modelManager.addClient(ALICE);
        assertTrue(modelManager.hasClient(ALICE));
    }

    @Test
    public void getFilteredClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredClientList().remove(0));
    }

    @Test
    public void hasPet_nullPet_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPet(null));
    }

    @Test
    public void hasPet_petNotInGroomingBook_returnsFalse() {
        assertFalse(modelManager.hasPet(new PetBuilder(ALICE).build()));
    }

    @Test
    public void addPet_petAdded_isPresentAndShown() {
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addClient(ALICE);

        modelManager.addPet(pet);

        assertTrue(modelManager.hasPet(pet));
        assertEquals(List.of(pet), modelManager.getFilteredPetList());
    }

    @Test
    public void updateFilteredPetList_filtersPets() {
        Pet dog = new PetBuilder(ALICE).withSpecies(Species.DOG).build();
        Pet cat = new PetBuilder(ALICE).withName("Luna").withSpecies(Species.CAT).build();
        modelManager.addClient(ALICE);
        modelManager.addPet(dog);
        modelManager.addPet(cat);

        modelManager.updateFilteredPetList(pet -> pet.getSpecies() == Species.CAT);

        assertEquals(List.of(cat), modelManager.getFilteredPetList());
        modelManager.updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
        assertEquals(List.of(dog, cat), modelManager.getFilteredPetList());
    }

    @Test
    public void deletePet_petInGroomingBook_removesPet() {
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addClient(ALICE);
        modelManager.addPet(pet);

        modelManager.deletePet(pet);

        assertFalse(modelManager.hasPet(pet));
        assertEquals(List.of(), modelManager.getFilteredPetList());
    }

    @Test
    public void getFilteredPetList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPetList().remove(0));
    }

    @Test
    public void equals() {
        GroomingBook clientBook = new GroomingBookBuilder().withClient(ALICE).withClient(BENSON).build();
        GroomingBook differentClientBook = new GroomingBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(clientBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(clientBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different groomingBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentClientBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredClientList(new ClientNameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(clientBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(clientBook, differentUserPrefs)));
    }

    @Test
    public void addPet_ownerNotInGroomingBook_throwsClientNotFoundException() {
        assertThrows(ClientNotFoundException.class, () -> modelManager.addPet(new PetBuilder(ALICE).build()));
    }

    @Test
    public void addPet_ownerInGroomingBook_addsPet() {
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addClient(ALICE);

        modelManager.addPet(pet);

        assertTrue(modelManager.hasPet(pet));
    }

    @Test
    public void getPetList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getGroomingBook().getPetList().remove(0));
    }

    @Test
    public void setClient_ownerOfPet_replacesPetOwner() {
        modelManager.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addPet(pet);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();

        modelManager.setClient(ALICE, editedAlice);

        assertSame(editedAlice, modelManager.getGroomingBook().getPetList().get(0).getOwner());
    }

    @Test
    public void constructor_copiesAllRecordsWithoutSharingMutableLists() {
        GroomingBook source = new GroomingBook();
        source.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        source.addPet(pet);
        ModelManager model = new ModelManager(source, new UserPrefs());

        source.addClient(BENSON);
        source.removePet(pet);

        assertEquals(List.of(ALICE), model.getGroomingBook().getClientList());
        assertEquals(List.of(pet), model.getGroomingBook().getPetList());
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
    }

    @Test
    public void setPet_ownerMissing_keepsExistingPet() {
        modelManager.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addPet(pet);

        assertThrows(ClientNotFoundException.class, () ->
                modelManager.setPet(pet, pet.withOwner(BENSON)));
        assertEquals(List.of(pet), modelManager.getGroomingBook().getPetList());
    }

    @Test
    public void setGroomingBook_ownerMissing_keepsAllRecordsUnchanged() {
        modelManager.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addPet(pet);
        ReadOnlyGroomingBook replacement = new GroomingBook() {
            @Override
            public javafx.collections.ObservableList<Pet> getPetList() {
                return javafx.collections.FXCollections.observableArrayList(new PetBuilder(BENSON).build());
            }
        };

        assertThrows(ClientNotFoundException.class, () -> modelManager.setGroomingBook(replacement));
        assertEquals(List.of(ALICE), modelManager.getGroomingBook().getClientList());
        assertEquals(List.of(pet), modelManager.getGroomingBook().getPetList());
    }

    @Test
    public void setClient_duplicateIdentity_keepsClientAndPetOwnerUnchanged() {
        modelManager.addClient(ALICE);
        modelManager.addClient(BENSON);
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addPet(pet);

        assertThrows(DuplicateClientException.class, () -> modelManager.setClient(ALICE, BENSON));
        assertEquals(List.of(ALICE, BENSON), modelManager.getGroomingBook().getClientList());
        assertSame(ALICE, modelManager.getGroomingBook().getPetList().get(0).getOwner());
    }

    @Test
    public void setGroomingBook_updatedOwner_refreshesPetReference() {
        modelManager.addClient(ALICE);
        modelManager.addPet(new PetBuilder(ALICE).build());
        Client updatedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        GroomingBook replacement = new GroomingBook();
        replacement.addClient(updatedAlice);

        replacement.addPet(new PetBuilder(ALICE).build());
        modelManager.setGroomingBook(replacement);

        assertSame(updatedAlice, modelManager.getGroomingBook().getPetList().get(0).getOwner());
        assertEquals(List.of(updatedAlice), modelManager.getFilteredClientList());
    }

    @Test
    public void setGroomingBook_validReplacement_updatesExistingFilteredView() {
        modelManager.addClient(ALICE);
        var filteredPets = modelManager.getFilteredPetList();
        Pet pet = new PetBuilder(ALICE).build();
        GroomingBook replacement = new GroomingBook();
        replacement.addClient(ALICE);
        replacement.addPet(pet);

        modelManager.setGroomingBook(replacement);
        replacement.removePet(pet);

        assertSame(filteredPets, modelManager.getFilteredPetList());
        assertEquals(List.of(pet), filteredPets);
    }
}
