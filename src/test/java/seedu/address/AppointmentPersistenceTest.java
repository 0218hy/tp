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
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.appointment.AddAppointmentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.GroomingBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
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
    public void createClientPetAndAppointment_throughCommands_preservesRecordsAfterRestart() throws Exception {
        logic.execute("add-client n/Jane Tan i/81234567 e/jane@example.com a/123 Clementi Road");
        logic.execute("add-pet p/Café 小白 i/81234567 s/Dog r/Calm with baths");
        String booking = "add-appointment i/81234567 p/CAFÉ   小白 "
                + "d/01-01-9999 st/10:00 et/11:00 s/Full groom";
        logic.execute(booking);

        Appointment expected = new Appointment(new Phone("81234567"),
                "CAFÉ 小白", new AppointmentDate("01-01-9999"), new StartTime("10:00"),
                new EndTime("11:00"), Service.FULL_GROOM);
        assertEquals(List.of(expected), model.getGroomingBook().getAppointmentList());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(model.getGroomingBook(), restarted.getGroomingBook());
        logicFor(restarted, storage).execute(booking.replace("01-01-9999", "02-01-9999"));
        assertEquals(2, restarted.getGroomingBook().getAppointmentList().size());
    }

    @Test
    public void deleteAppointment_savesUpdatedSchedule() throws Exception {
        logic.execute(COMMAND);

        logic.execute("delete-appointment d/01-01-9999 t/10:30");

        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
        assertTrue(storage.readGroomingBook().orElseThrow().getAppointmentList().isEmpty());
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
    public void clearAllRecords_includingFilteredAndHistoricalRecords_persistsAfterRestart() throws Exception {
        model.setAppointments(historicalBook().getAppointmentList());
        logic.execute(COMMAND);
        var clients = model.getFilteredClientList();
        var pets = model.getFilteredPetList();
        var appointments = model.getFilteredAppointmentList();
        model.updateFilteredClientList(unused -> false);
        model.updateFilteredPetList(unused -> false);
        model.updateFilteredAppointmentList(unused -> false);

        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear").getFeedbackToUser());

        assertEquals(new GroomingBook(), model.getGroomingBook());
        model.updateFilteredClientList(Model.PREDICATE_SHOW_ALL_CLIENTS);
        model.updateFilteredPetList(Model.PREDICATE_SHOW_ALL_PETS);
        model.updateFilteredAppointmentList(Model.PREDICATE_SHOW_ALL_APPOINTMENTS);
        assertTrue(clients.isEmpty());
        assertTrue(pets.isEmpty());
        assertTrue(appointments.isEmpty());
        assertEquals(model.getGroomingBook(), storage.readGroomingBook().orElseThrow());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(new GroomingBook(), restarted.getGroomingBook());
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getGroomingBookFilePath()), paths.toList());
        }
    }

    @Test
    public void clearSaveFails_restoresAllRecordsAndPreservesSavedFile() throws Exception {
        model.setAppointments(historicalBook().getAppointmentList());
        logic.execute(COMMAND);
        GroomingBook before = new GroomingBook(model.getGroomingBook());
        String savedBefore = Files.readString(storage.getGroomingBookFilePath());
        JsonGroomingBookStorage failingStorage = new JsonGroomingBookStorage(storage.getGroomingBookFilePath()) {
            @Override
            public void saveGroomingBook(ReadOnlyGroomingBook groomingBook) throws IOException {
                throw new IOException("write failed");
            }
        };
        LogicManager failingLogic = logicFor(model, storageWith(failingStorage));

        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute("clear"));

        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "write failed"), exception.getMessage());
        assertEquals(before, model.getGroomingBook());
        assertEquals(savedBefore, Files.readString(storage.getGroomingBookFilePath()));
    }

    @Test
    public void scheduleWithMissingPet_rejectsAndDoesNotSave() throws Exception {
        String before = Files.readString(storage.getGroomingBookFilePath());
        model.deletePet(model.getGroomingBook().getPetList().getFirst());
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(COMMAND));
        assertEquals(AddAppointmentCommand.MESSAGE_PET_NOT_FOUND, exception.getMessage());
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
        return new LogicManager(target, targetStorage);
    }

    private GroomingBook historicalBook() {
        GroomingBook book = new GroomingBook();
        book.addAppointment(new Appointment(ALICE.getPhone(), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        return book;
    }
}
