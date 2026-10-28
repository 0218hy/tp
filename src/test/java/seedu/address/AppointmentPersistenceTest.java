package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
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
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.storage.JsonBubuBookStorage;
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

    //@@author 0218hy
    @BeforeEach
    public void setUp() throws Exception {
        storage = storageWith(new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json")));
        BubuBook owners = new BubuBook();
        owners.addClient(ALICE);
        owners.addPet(new PetBuilder(ALICE).withName("BuBu").withBreed("Golden retriever").build());
        storage.saveBubuBook(owners);
        model = new MainApp().initModelManager(storage, new UserPrefs());
        logic = logicFor(model, storage);
    }

    @Test
    public void scheduleSaveAndRestart_preservesAppointmentsAndDetectsOverlap() throws Exception {
        BubuBook before = new BubuBook(model.getBubuBook());
        logic.execute(COMMAND);
        assertEquals(before.getClientList(), model.getBubuBook().getClientList());
        assertEquals(before.getPetList(), model.getBubuBook().getPetList());
        assertEquals(model.getBubuBook().getAppointmentList(),
                storage.readBubuBook().orElseThrow().getAppointmentList());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(model.getBubuBook(), restarted.getBubuBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                logicFor(restarted, storage).execute(COMMAND));
        assertEquals(AddAppointmentCommand.MESSAGE_OVERLAP, exception.getMessage());
        assertEquals(1, restarted.getBubuBook().getAppointmentList().size());
    }

    @Test
    public void createClientPetAndAppointment_throughCommands_preservesRecordsAfterRestart() throws Exception {
        logic.execute("add-client n/Jane Tan i/81234567 e/jane@example.com a/123 Clementi Road");
        logic.execute("add-pet p/Café 小白 i/81234567 s/Dog r/Calm with baths");
        String booking = "add-appointment i/81234567 p/CAFÉ   小白 "
                + "d/01-01-9999 st/10:00 et/11:00 s/Full groom";
        logic.execute(booking);

        var owner = model.getBubuBook().getClientList().stream()
                .filter(client -> client.getPhone().value.equals("81234567")).findFirst().orElseThrow();
        var pet = model.getBubuBook().getPetList().stream()
                .filter(candidate -> candidate.getOwner().equals(owner)).findFirst().orElseThrow();
        Appointment expected = new Appointment(owner, pet, new AppointmentDate("01-01-9999"), new StartTime("10:00"),
                new EndTime("11:00"), Service.FULL_GROOM);
        assertEquals(List.of(expected), model.getBubuBook().getAppointmentList());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(model.getBubuBook(), restarted.getBubuBook());
        logicFor(restarted, storage).execute(booking.replace("01-01-9999", "02-01-9999"));
        assertEquals(2, restarted.getBubuBook().getAppointmentList().size());
    }
    //@@author

    //@@author chunghean
    @Test
    public void deleteAppointment_savesUpdatedSchedule() throws Exception {
        logic.execute(COMMAND);

        logic.execute("delete-appointment d/01-01-9999 st/10:00");

        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
        assertTrue(storage.readBubuBook().orElseThrow().getAppointmentList().isEmpty());
    }
    //@@author

    //@@author 0218hy
    @Test
    public void startup_missingBubuBook_usesSampleDataWithoutCreatingFiles() throws Exception {
        Files.delete(storage.getBubuBookFilePath());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getBubuBook().getAppointmentList().isEmpty());
        assertFalse(restarted.getBubuBook().getClientList().isEmpty());
        assertFalse(Files.exists(storage.getBubuBookFilePath()));
    }

    @Test
    public void startup_invalidBubuBook_preservesFileAndBlocksCommands() throws Exception {
        Files.writeString(storage.getBubuBookFilePath(), "broken data");
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(new BubuBook(), restarted.getBubuBook());
        LogicManager corruptedLogic = new LogicManager(restarted, storage, true);
        CommandException exception = assertThrows(CommandException.class, () -> corruptedLogic.execute("clear"));
        assertEquals(LogicManager.MESSAGE_CORRUPTED_RECORDS, exception.getMessage());
        assertEquals("broken data", Files.readString(storage.getBubuBookFilePath()));
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

        assertEquals(new BubuBook(), model.getBubuBook());
        model.updateFilteredClientList(Model.PREDICATE_SHOW_ALL_CLIENTS);
        model.updateFilteredPetList(Model.PREDICATE_SHOW_ALL_PETS);
        model.updateFilteredAppointmentList(Model.PREDICATE_SHOW_ALL_APPOINTMENTS);
        assertTrue(clients.isEmpty());
        assertTrue(pets.isEmpty());
        assertTrue(appointments.isEmpty());
        assertEquals(model.getBubuBook(), storage.readBubuBook().orElseThrow());
        Model restarted = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(new BubuBook(), restarted.getBubuBook());
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getBubuBookFilePath()), paths.toList());
        }
    }

    @Test
    public void clearSaveFails_restoresAllRecordsAndPreservesSavedFile() throws Exception {
        model.setAppointments(historicalBook().getAppointmentList());
        logic.execute(COMMAND);
        BubuBook before = new BubuBook(model.getBubuBook());
        String savedBefore = Files.readString(storage.getBubuBookFilePath());
        JsonBubuBookStorage failingStorage = new JsonBubuBookStorage(storage.getBubuBookFilePath()) {
            @Override
            public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
                throw new IOException("write failed");
            }
        };
        LogicManager failingLogic = logicFor(model, storageWith(failingStorage));

        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute("clear"));

        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "write failed"), exception.getMessage());
        assertEquals(before, model.getBubuBook());
        assertEquals(savedBefore, Files.readString(storage.getBubuBookFilePath()));
    }

    @Test
    public void scheduleWithMissingPet_rejectsAndDoesNotSave() throws Exception {
        String before = Files.readString(storage.getBubuBookFilePath());
        model.deletePet(model.getBubuBook().getPetList().getFirst());
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(COMMAND));
        assertEquals(AddAppointmentCommand.MESSAGE_PET_NOT_FOUND, exception.getMessage());
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
        assertEquals(before, Files.readString(storage.getBubuBookFilePath()));
    }

    @Test
    public void scheduleWithInvalidArguments_doesNotSave() throws Exception {
        String before = Files.readString(storage.getBubuBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute(COMMAND.replace("10:00", "10:15")));
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
        assertEquals(before, Files.readString(storage.getBubuBookFilePath()));
    }

    @Test
    public void scheduleSaveFails_restoresModelAndAllowsRetry() throws Exception {
        assertSaveFailure(new IOException("write failed"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "write failed"));
    }

    @Test
    public void scheduleSaveDenied_restoresModelAndReportsPermissionError() throws Exception {
        assertSaveFailure(new AccessDeniedException("bububook.json"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "bububook.json"));
    }

    private void assertSaveFailure(IOException failure, String expectedMessage) throws Exception {
        BubuBook before = historicalBook();
        model.setAppointments(before.getAppointmentList());
        storage.saveBubuBook(model.getBubuBook());
        String savedBefore = Files.readString(storage.getBubuBookFilePath());
        JsonBubuBookStorage failOnce = new JsonBubuBookStorage(storage.getBubuBookFilePath()) {
            private boolean failNext = true;

            @Override
            public void saveBubuBook(ReadOnlyBubuBook appointmentBook) throws IOException {
                if (failNext) {
                    failNext = false;
                    throw failure;
                }
                super.saveBubuBook(appointmentBook);
            }
        };
        BubuBook allRecordsBefore = new BubuBook(model.getBubuBook());
        LogicManager failingLogic = logicFor(model, storageWith(failOnce));
        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute(COMMAND));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(allRecordsBefore, model.getBubuBook());
        assertEquals(savedBefore, Files.readString(storage.getBubuBookFilePath()));
        failingLogic.execute(COMMAND);
        assertEquals(2, model.getBubuBook().getAppointmentList().size());
        assertEquals(model.getBubuBook().getAppointmentList(),
                storage.readBubuBook().orElseThrow().getAppointmentList());
    }

    private StorageManager storageWith(JsonBubuBookStorage bubuBookStorage) {
        return new StorageManager(bubuBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private LogicManager logicFor(Model target, StorageManager targetStorage) {
        return new LogicManager(target, targetStorage);
    }

    private BubuBook historicalBook() {
        BubuBook book = new BubuBook();
        book.addAppointment(createAppointment(ALICE.getPhone(), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        return book;
    }
    //@@author
}
