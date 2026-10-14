package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.appointment.ScheduleAppointmentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.BuBuParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.GroomingBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentParticipantLookup;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;
import seedu.address.storage.JsonClientPetStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.appointment.AppointmentStorage;
import seedu.address.storage.appointment.JsonAppointmentStorage;
import seedu.address.testutil.PetBuilder;

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
        storage = storageWith(new JsonAppointmentStorage(temporaryFolder.resolve("appointments.json")));
        GroomingBook owners = new GroomingBook();
        owners.addClient(ALICE);
        owners.addPet(new PetBuilder(ALICE).withName("BuBu").withBreed("Golden retriever").build());
        storage.saveClientPetData(owners);
        model = new MainApp().initModelManager(storage, new UserPrefs());
        logic = logicFor(model, storage);
    }

    @Test
    public void scheduleSaveAndRestart_preservesAppointmentsAndDetectsOverlap() throws Exception {
        String ownersBefore = Files.readString(storage.getClientPetFilePath());
        logic.execute(COMMAND);
        assertEquals(ownersBefore, Files.readString(storage.getClientPetFilePath()));
        assertEquals(model.getGroomingBook().getAppointmentList(),
                storage.readAppointmentData().orElseThrow().getAppointmentList());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(model.getGroomingBook(), restarted.getGroomingBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                logicFor(restarted, storage).execute(COMMAND));
        assertEquals(ScheduleAppointmentCommand.MESSAGE_OVERLAP, exception.getMessage());
        assertEquals(1, restarted.getGroomingBook().getAppointmentList().size());
    }

    @Test
    public void startup_missingAppointmentFile_startsEmptyWithoutCreatingFile() {
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
        assertFalse(Files.exists(storage.getAppointmentFilePath()));
        assertEquals(List.of(ALICE), model.getGroomingBook().getClientList());
    }

    @Test
    public void startup_invalidAppointmentFile_preservesFileAndLoadsOwners() throws Exception {
        Files.writeString(storage.getAppointmentFilePath(), "broken appointment data");
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getGroomingBook().getAppointmentList().isEmpty());
        assertEquals(List.of(ALICE), restarted.getGroomingBook().getClientList());
        new LogicManager(restarted, storage).execute("list");
        assertEquals("broken appointment data", Files.readString(storage.getAppointmentFilePath()));
    }

    @Test
    public void startup_invalidOwnerFile_stillLoadsHistoricalAppointments() throws Exception {
        GroomingBook appointments = historicalBook();
        storage.saveAppointmentData(appointments);
        Files.writeString(storage.getClientPetFilePath(), "broken owner data");
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getGroomingBook().getClientList().isEmpty());
        assertEquals(appointments, restarted.getGroomingBook());
    }

    @Test
    public void clearContacts_leavesScheduleAndAppointmentFileUnchanged() throws Exception {
        logic.execute(COMMAND);
        String appointmentsBefore = Files.readString(storage.getAppointmentFilePath());
        logic.execute("clear");
        assertTrue(model.getGroomingBook().getClientList().isEmpty());
        assertEquals(1, model.getGroomingBook().getAppointmentList().size());
        assertEquals(appointmentsBefore, Files.readString(storage.getAppointmentFilePath()));
    }

    @Test
    public void scheduleWithoutLookup_reportsPendingIntegrationAndDoesNotSave() {
        LogicManager defaultLogic = new LogicManager(model, storage);
        ParseException exception = assertThrows(ParseException.class, () -> defaultLogic.execute(COMMAND));
        assertEquals(BuBuParser.MESSAGE_SCHEDULING_UNAVAILABLE, exception.getMessage());
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
        assertFalse(Files.exists(storage.getAppointmentFilePath()));
    }

    @Test
    public void scheduleWithInvalidArguments_doesNotSave() {
        assertThrows(ParseException.class, () -> logic.execute(COMMAND.replace("10:00", "10:15")));
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
        assertFalse(Files.exists(storage.getAppointmentFilePath()));
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
        GroomingBook before = historicalBook();
        model.setAppointments(before.getAppointmentList());
        storage.saveAppointmentData(before);
        String savedBefore = Files.readString(storage.getAppointmentFilePath());
        JsonAppointmentStorage failOnce = new JsonAppointmentStorage(storage.getAppointmentFilePath()) {
            private boolean failNext = true;

            @Override
            public void saveAppointmentData(ReadOnlyGroomingBook appointmentBook) throws IOException {
                if (failNext) {
                    failNext = false;
                    throw failure;
                }
                super.saveAppointmentData(appointmentBook);
            }
        };
        GroomingBook allRecordsBefore = new GroomingBook(model.getGroomingBook());
        LogicManager failingLogic = logicFor(model, storageWith(failOnce));
        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute(COMMAND));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(allRecordsBefore, model.getGroomingBook());
        assertEquals(savedBefore, Files.readString(storage.getAppointmentFilePath()));
        failingLogic.execute(COMMAND);
        assertEquals(2, model.getGroomingBook().getAppointmentList().size());
        assertEquals(model.getGroomingBook().getAppointmentList(),
                storage.readAppointmentData().orElseThrow().getAppointmentList());
    }

    private StorageManager storageWith(AppointmentStorage appointmentStorage) {
        return new StorageManager(new JsonClientPetStorage(temporaryFolder.resolve("addressbook.json")),
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
        return new LogicManager(target, targetStorage, new BuBuParser(lookup));
    }

    private GroomingBook historicalBook() {
        GroomingBook book = new GroomingBook();
        book.addAppointment(new Appointment(ALICE.getPhone(), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        return book;
    }
}
