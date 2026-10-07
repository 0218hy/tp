package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.appointment.ScheduleCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentBook;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentParticipantLookup;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.ReadOnlyAppointmentBook;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.appointment.AppointmentStorage;
import seedu.address.storage.appointment.JsonAppointmentBookStorage;

public class AppointmentPersistenceTest {
    private static final String COMMAND = "schedule i/" + ALICE.getPhone()
            + " p/BuBu d/01-01-9999 st/10:00 et/11:00 s/Full groom";

    @TempDir
    public Path temporaryFolder;

    private StorageManager storage;
    private Model model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        storage = storageWith(new JsonAppointmentBookStorage(temporaryFolder.resolve("appointments.json")));
        AddressBook owners = new AddressBook();
        owners.addPerson(ALICE);
        storage.saveAddressBook(owners);
        model = new MainApp().initModelManager(storage, new UserPrefs());
        logic = logicFor(model, storage);
    }

    @Test
    public void scheduleSaveAndRestart_preservesAppointmentsAndDetectsOverlap() throws Exception {
        String ownersBefore = Files.readString(storage.getAddressBookFilePath());
        logic.execute(COMMAND);
        assertEquals(ownersBefore, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(model.getAppointmentBook(), storage.readAppointmentBook().orElseThrow());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(model.getAppointmentBook(), restarted.getAppointmentBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                logicFor(restarted, storage).execute(COMMAND));
        assertEquals(ScheduleCommand.MESSAGE_OVERLAP, exception.getMessage());
        assertEquals(1, restarted.getAppointmentBook().getAppointmentList().size());
    }

    @Test
    public void startup_missingAppointmentFile_startsEmptyWithoutCreatingFile() {
        assertTrue(model.getAppointmentBook().getAppointmentList().isEmpty());
        assertFalse(Files.exists(storage.getAppointmentBookFilePath()));
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void startup_invalidAppointmentFile_preservesFileAndLoadsOwners() throws Exception {
        Files.writeString(storage.getAppointmentBookFilePath(), "broken appointment data");
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getAppointmentBook().getAppointmentList().isEmpty());
        assertEquals(List.of(ALICE), restarted.getAddressBook().getPersonList());
        new LogicManager(restarted, storage).execute("list");
        assertEquals("broken appointment data", Files.readString(storage.getAppointmentBookFilePath()));
    }

    @Test
    public void startup_invalidOwnerFile_stillLoadsHistoricalAppointments() throws Exception {
        AppointmentBook appointments = historicalBook();
        storage.saveAppointmentBook(appointments);
        Files.writeString(storage.getAddressBookFilePath(), "broken owner data");
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getAddressBook().getPersonList().isEmpty());
        assertEquals(appointments, restarted.getAppointmentBook());
    }

    @Test
    public void clearContacts_leavesScheduleAndAppointmentFileUnchanged() throws Exception {
        logic.execute(COMMAND);
        String appointmentsBefore = Files.readString(storage.getAppointmentBookFilePath());
        logic.execute("clear");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(1, model.getAppointmentBook().getAppointmentList().size());
        assertEquals(appointmentsBefore, Files.readString(storage.getAppointmentBookFilePath()));
    }

    @Test
    public void scheduleWithoutLookup_reportsPendingIntegrationAndDoesNotSave() {
        LogicManager defaultLogic = new LogicManager(model, storage);
        ParseException exception = assertThrows(ParseException.class, () -> defaultLogic.execute(COMMAND));
        assertEquals(AddressBookParser.MESSAGE_SCHEDULING_UNAVAILABLE, exception.getMessage());
        assertTrue(model.getAppointmentBook().getAppointmentList().isEmpty());
        assertFalse(Files.exists(storage.getAppointmentBookFilePath()));
    }

    @Test
    public void scheduleWithInvalidArguments_doesNotSave() {
        assertThrows(ParseException.class, () -> logic.execute(COMMAND.replace("10:00", "10:15")));
        assertTrue(model.getAppointmentBook().getAppointmentList().isEmpty());
        assertFalse(Files.exists(storage.getAppointmentBookFilePath()));
    }

    @Test
    public void scheduleSaveFails_restoresModelAndAllowsRetry() throws Exception {
        assertSaveFailure(new IOException("write failed"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "write failed"));
    }

    @Test
    public void scheduleSaveDenied_restoresModelAndReportsPermissionError() throws Exception {
        assertSaveFailure(new AccessDeniedException("appointments.json"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "appointments.json"));
    }

    private void assertSaveFailure(IOException failure, String expectedMessage) throws Exception {
        AppointmentBook before = historicalBook();
        model.setAppointmentBook(before);
        storage.saveAppointmentBook(before);
        String savedBefore = Files.readString(storage.getAppointmentBookFilePath());
        JsonAppointmentBookStorage failOnce = new JsonAppointmentBookStorage(storage.getAppointmentBookFilePath()) {
            private boolean failNext = true;

            @Override
            public void saveAppointmentBook(ReadOnlyAppointmentBook appointmentBook) throws IOException {
                if (failNext) {
                    failNext = false;
                    throw failure;
                }
                super.saveAppointmentBook(appointmentBook);
            }
        };
        LogicManager failingLogic = logicFor(model, storageWith(failOnce));
        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute(COMMAND));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(before, model.getAppointmentBook());
        assertEquals(savedBefore, Files.readString(storage.getAppointmentBookFilePath()));
        failingLogic.execute(COMMAND);
        assertEquals(2, model.getAppointmentBook().getAppointmentList().size());
        assertEquals(model.getAppointmentBook(), storage.readAppointmentBook().orElseThrow());
    }

    private StorageManager storageWith(AppointmentStorage appointmentStorage) {
        return new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")), appointmentStorage);
    }

    private LogicManager logicFor(Model target, StorageManager targetStorage) {
        // A test-only registry; production must supply the real owner/pet feature implementation.
        AppointmentParticipantLookup lookup = new AppointmentParticipantLookup() {
            @Override
            public boolean hasOwner(Phone ownerPhone) {
                return ownerPhone.equals(ALICE.getPhone());
            }

            @Override
            public boolean hasPet(Phone ownerPhone, String petName) {
                return hasOwner(ownerPhone) && petName.equals("BuBu");
            }
        };
        return new LogicManager(target, targetStorage, new AddressBookParser(lookup));
    }

    private AppointmentBook historicalBook() {
        AppointmentBook book = new AppointmentBook();
        book.addAppointment(new Appointment(ALICE.getPhone(), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        return book;
    }
}
