package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CLIENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.ClientNameContainsKeywordsPredicate;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.Species;
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
    public void deleteAppointment_appointmentInGroomingBook_removesAppointment() {
        Appointment appointment = new Appointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("01-01-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        modelManager.addAppointment(appointment);

        modelManager.deleteAppointment(appointment);

        assertTrue(modelManager.getGroomingBook().getAppointmentList().isEmpty());
    }

    @Test
    public void getFilteredPetList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPetList().remove(0));
    }

    @Test
    public void equals() {
        GroomingBook groomingBook = new GroomingBookBuilder().withClient(ALICE).withClient(BENSON).build();
        GroomingBook differentGroomingBook = new GroomingBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(groomingBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(groomingBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different groomingBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentGroomingBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredClientList(new ClientNameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(groomingBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(groomingBook, differentUserPrefs)));
    }
}
