package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CLIENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
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
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.Species;
import seedu.address.testutil.BubuBookBuilder;
import seedu.address.testutil.PetBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new BubuBook(), new BubuBook(modelManager.getBubuBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new BubuBook(), userPrefs);
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
    public void hasClient_clientNotInBubuBook_returnsFalse() {
        assertFalse(modelManager.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInBubuBook_returnsTrue() {
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
    public void hasPet_petNotInBubuBook_returnsFalse() {
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
        assertEquals(List.of(cat, dog), modelManager.getFilteredPetList());
    }

    @Test
    public void deletePet_petInBubuBook_removesPet() {
        Pet pet = new PetBuilder(ALICE).build();
        modelManager.addClient(ALICE);
        modelManager.addPet(pet);

        modelManager.deletePet(pet);

        assertFalse(modelManager.hasPet(pet));
        assertEquals(List.of(), modelManager.getFilteredPetList());
    }

    @Test
    public void deleteAppointment_appointmentInBubuBook_removesAppointment() {
        Appointment appointment = createAppointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("01-01-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        modelManager.addAppointment(appointment);

        modelManager.deleteAppointment(appointment);

        assertTrue(modelManager.getBubuBook().getAppointmentList().isEmpty());
    }

    @Test
    public void updateFilteredAppointmentList_filtersAppointments() {
        Appointment firstAppointment = createAppointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("01-01-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        Appointment secondAppointment = createAppointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("02-01-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        modelManager.setAppointments(List.of(firstAppointment, secondAppointment));

        modelManager.updateFilteredAppointmentList(appointment -> appointment.equals(secondAppointment));

        assertEquals(List.of(secondAppointment), modelManager.getFilteredAppointmentList());
    }

    @Test
    public void addAndDeleteAppointment_updatesFilteredAppointmentList() {
        Appointment appointment = createAppointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("01-01-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);

        modelManager.addAppointment(appointment);
        assertEquals(List.of(appointment), modelManager.getFilteredAppointmentList());

        modelManager.deleteAppointment(appointment);
        assertTrue(modelManager.getFilteredAppointmentList().isEmpty());
    }

    @Test
    public void getFilteredAppointmentList_modifyList_throwsUnsupportedOperationException() {
        Appointment appointment = createAppointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("01-01-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        modelManager.addAppointment(appointment);

        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredAppointmentList().remove(0));
    }

    @Test
    public void getFilteredPetList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPetList().remove(0));
    }

    @Test
    public void equals() {
        BubuBook bubuBook = new BubuBookBuilder().withClient(ALICE).withClient(BENSON).build();
        BubuBook differentBubuBook = new BubuBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(bubuBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(bubuBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different bubuBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentBubuBook, userPrefs)));

        // different filteredList -> returns false
        modelManager.updateFilteredClientList(ALICE::equals);
        assertFalse(modelManager.equals(new ModelManager(bubuBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(bubuBook, differentUserPrefs)));
    }
}
