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
import seedu.address.logic.commands.appointment.AddAppointmentCommand;
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
import seedu.address.storage.JsonGroomingBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PetBuilder;

public class AppointmentPersistenceTest {
    private static final String COMMAND = "add-appointment i/" + ALICE.getPhone()
            + " p/BuBu d/01-01-9999 st/10:00 et/11:00 s/Full groom";

    @TempDir
    public Path temporaryFolder;

    private StorageManager storage;
    private Model model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        storage = storageWith(new JsonGroomingBookStorage(temporaryFolder.resolve("groombook.json")));
        GroomingBook owners = new GroomingBook();
        owners.addClient(ALICE);
        owners.addPet(new PetBuilder(ALICE).withName("BuBu").withBreed("Golden retriever").build());
        storage.saveGroomingBook(owners);
        model = new MainApp().initModelManager(storage, new UserPrefs());
        logic = logicFor(model, storage);
    }

    @Test
    public void scheduleSaveAndRestart_preservesAppointmentsAndDetectsOverlap() throws Exception {
        GroomingBook before = new GroomingBook(model.getGroomingBook());
        logic.execute(COMMAND);
        assertEquals(before.getClientList(), model.getGroomingBook().getClientList());
        assertEquals(before.getPetList(), model.getGroomingBook().getPetList());
        assertEquals(model.getGroomingBook().getAppointmentList(),
                storage.readGroomingBook().orElseThrow().getAppointmentList());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(model.getGroomingBook(), restarted.getGroomingBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                logicFor(restarted, storage).execute(COMMAND));
        assertEquals(AddAppointmentCommand.MESSAGE_OVERLAP, exception.getMessage());
        assertEquals(1, restarted.getGroomingBook().getAppointmentList().size());
    }

    @Test
    public void startup_missingGroomingBook_usesSampleDataWithoutCreatingFiles() throws Exception {
        Files.delete(storage.getGroomingBookFilePath());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getGroomingBook().getAppointmentList().isEmpty());
        assertFalse(restarted.getGroomingBook().getClientList().isEmpty());
        assertFalse(Files.exists(storage.getGroomingBookFilePath()));
    }

    @Test
    public void startup_invalidGroomingBook_preservesFileAndBlocksCommands() throws Exception {
        Files.writeString(storage.getGroomingBookFilePath(), "broken data");
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(new GroomingBook(), restarted.getGroomingBook());
        LogicManager corruptedLogic = new LogicManager(restarted, storage, true);
        CommandException exception = assertThrows(CommandException.class, () -> corruptedLogic.execute("clear"));
        assertEquals(LogicManager.MESSAGE_CORRUPTED_RECORDS, exception.getMessage());
        assertEquals("broken data", Files.readString(storage.getGroomingBookFilePath()));
    }

    @Test
    public void clearContacts_savesEmptyContactsAndRetainsScheduleInSameFile() throws Exception {
        logic.execute(COMMAND);
        logic.execute("clear");
        assertTrue(model.getGroomingBook().getClientList().isEmpty());
        assertTrue(model.getGroomingBook().getPetList().isEmpty());
        assertEquals(1, model.getGroomingBook().getAppointmentList().size());
        assertEquals(model.getGroomingBook(), storage.readGroomingBook().orElseThrow());
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getGroomingBookFilePath()), paths.toList());
        }
    }

    @Test
    public void scheduleWithoutLookup_reportsPendingIntegrationAndDoesNotSave() throws Exception {
        String before = Files.readString(storage.getGroomingBookFilePath());
        LogicManager defaultLogic = new LogicManager(model, storage);
        ParseException exception = assertThrows(ParseException.class, () -> defaultLogic.execute(COMMAND));
        assertEquals(BuBuParser.MESSAGE_SCHEDULING_UNAVAILABLE, exception.getMessage());
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
        assertEquals(before, Files.readString(storage.getGroomingBookFilePath()));
    }

    @Test
    public void scheduleWithInvalidArguments_doesNotSave() throws Exception {
        String before = Files.readString(storage.getGroomingBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute(COMMAND.replace("10:00", "10:15")));
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
        assertEquals(before, Files.readString(storage.getGroomingBookFilePath()));
    }

    @Test
    public void scheduleSaveFails_restoresModelAndAllowsRetry() throws Exception {
        assertSaveFailure(new IOException("write failed"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "write failed"));
    }

    @Test
    public void scheduleSaveDenied_restoresModelAndReportsPermissionError() throws Exception {
        assertSaveFailure(new AccessDeniedException("groombook.json"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "groombook.json"));
    }

    private void assertSaveFailure(IOException failure, String expectedMessage) throws Exception {
        GroomingBook before = historicalBook();
        model.setAppointments(before.getAppointmentList());
        storage.saveGroomingBook(model.getGroomingBook());
        String savedBefore = Files.readString(storage.getGroomingBookFilePath());
        JsonGroomingBookStorage failOnce = new JsonGroomingBookStorage(storage.getGroomingBookFilePath()) {
            private boolean failNext = true;

            @Override
            public void saveGroomingBook(ReadOnlyGroomingBook appointmentBook) throws IOException {
                if (failNext) {
                    failNext = false;
                    throw failure;
                }
                super.saveGroomingBook(appointmentBook);
            }
        };
        GroomingBook allRecordsBefore = new GroomingBook(model.getGroomingBook());
        LogicManager failingLogic = logicFor(model, storageWith(failOnce));
        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute(COMMAND));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(allRecordsBefore, model.getGroomingBook());
        assertEquals(savedBefore, Files.readString(storage.getGroomingBookFilePath()));
        failingLogic.execute(COMMAND);
        assertEquals(2, model.getGroomingBook().getAppointmentList().size());
        assertEquals(model.getGroomingBook().getAppointmentList(),
                storage.readGroomingBook().orElseThrow().getAppointmentList());
    }

    private StorageManager storageWith(JsonGroomingBookStorage groomingBookStorage) {
        return new StorageManager(groomingBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
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
