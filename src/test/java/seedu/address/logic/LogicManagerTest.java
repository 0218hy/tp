package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.ListClientsCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.pet.DeletePetCommand;
import seedu.address.logic.commands.pet.EditPetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.storage.JsonBubuBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonBubuBookStorage bubuBookStorage =
                new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(bubuBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    //@@author anderschow
    @Test
    public void execute_editClientFromFilteredList_updatesLinksAndSaves() throws Exception {
        model.addClient(AMY);
        model.addClient(ALICE);
        model.addPet(new PetBuilder(ALICE).build());
        model.addAppointment(createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-9999"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        logic.execute("list-clients q/" + ALICE.getPhone());

        Client edited = new ClientBuilder(ALICE).withName("Zoe Tan").withPhone("89998888").build();
        CommandResult result = logic.execute("edit-client i/" + ALICE.getPhone() + " n/Zoe Tan p/89998888");

        assertEquals(String.format(EditClientCommand.MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(edited)),
                result.getFeedbackToUser());
        assertEquals(List.of(AMY, edited), model.getFilteredClientList());
        assertEquals(edited, model.getBubuBook().getPetList().getFirst().getOwner());
        assertEquals(edited.getPhone(), model.getBubuBook().getAppointmentList().getFirst().getOwnerPhone());
        assertEquals(edited.getPhone(),
                model.getBubuBook().getAppointmentList().getFirst().getPet().getOwner().getPhone());
        assertEquals(model.getBubuBook(), new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json"))
                .readBubuBook().orElseThrow());
        assertThrows(CommandException.class, EditClientCommand.MESSAGE_CLIENT_NOT_FOUND, () ->
                logic.execute("edit-client i/" + ALICE.getPhone() + " n/Alex Tan"));
        logic.execute("edit-client i/89998888 n/Alex Tan");
        assertEquals("Alex Tan", model.getBubuBook().getClientList().get(1).getName().fullName);
    }

    @Test
    public void execute_editClientFromOtherPanels_displaysClients() throws Exception {
        model.addClient(ALICE);
        for (String command : List.of("list-pets", "list-appointments")) {
            logic.execute(command);
            CommandResult result = logic.execute("edit-client i/" + ALICE.getPhone() + " n/Zoe Tan");
            assertEquals(CommandResult.DisplayType.CLIENTS, result.getDisplayType());
            assertEquals("Zoe Tan", model.getFilteredClientList().getFirst().getName().fullName);
        }
    }
    @Test
    public void execute_editClientRejectsHiddenDuplicatePhoneAndEmail() throws Exception {
        model.addClient(ALICE);
        model.addClient(AMY);
        logic.execute("list-clients q/" + ALICE.getPhone());
        for (String field : List.of("p/" + AMY.getPhone(), "e/" + AMY.getEmail())) {
            assertThrows(CommandException.class, EditClientCommand.MESSAGE_DUPLICATE_CLIENT, () ->
                    logic.execute("edit-client i/" + ALICE.getPhone() + " " + field));
            assertEquals(List.of(ALICE), model.getFilteredClientList());
            assertEquals(List.of(ALICE, AMY), model.getBubuBook().getClientList());
        }
    }

    @Test
    public void execute_editClientSaveFails_restoresLinkedRecords() {
        model.addClient(ALICE);
        model.addPet(new PetBuilder(ALICE).build());
        model.addAppointment(createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-9999"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        JsonBubuBookStorage failingStorage = new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json")) {
            @Override
            public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        assertCommandException("edit-client i/" + ALICE.getPhone() + " p/89998888",
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }
    //@@author

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteClientCommand = "delete-client 9";
        assertCommandException(deleteClientCommand, MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListClientsCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListClientsCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addPet_parsesAddsAndSavesProfile() throws Exception {
        model.addClient(ALICE);

        CommandResult result = logic.execute("add-pet p/Mochi i/" + ALICE.getPhone()
                + " s/Cat r/Calm with baths");

        assertEquals("Pet added: Mochi (Cat) under Alice Pauline.", result.getFeedbackToUser());
        assertEquals("Mochi", model.getBubuBook().getPetList().getFirst().getName().value);
        assertEquals(model.getBubuBook(), new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json"))
                .readBubuBook().orElseThrow());
    }

    //@@author 0218hy
    @Test
    public void execute_listAppointments_readsScheduleWithoutSaving() throws Exception {
        model.addAppointment(createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));

        CommandResult result = logic.execute("list-appointments");

        assertEquals(ListAppointmentsCommand.MESSAGE_NO_APPOINTMENTS, result.getFeedbackToUser());
        assertEquals(1, model.getBubuBook().getAppointmentList().size());
        assertFalse(temporaryFolder.resolve("bububook.json").toFile().exists());
    }

    @Test
    public void execute_listPets_readsProfilesWithoutSaving() throws Exception {
        model.addClient(ALICE);
        model.addPet(new PetBuilder(ALICE).withName("Mochi").build());

        CommandResult result = logic.execute("list-pets i/" + ALICE.getPhone());

        assertEquals("Found 1 pet(s) under Alice Pauline.", result.getFeedbackToUser());
        assertEquals(1, model.getBubuBook().getPetList().size());
        assertFalse(temporaryFolder.resolve("bububook.json").toFile().exists());
    }
    //@@author

    @Test
    public void execute_indexDeletionRequiresMatchingDisplayedList() throws Exception {
        Appointment appointment = createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-9999"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        model.addAppointment(appointment);

        logic.execute("list-pets");
        assertCommandException("delete-appointment 1", LogicManager.MESSAGE_INVALID_DISPLAY_TYPE);

        logic.execute("list-appointments");
        String appointmentDetails = "Milo; Owner phone: " + ALICE.getPhone()
                + "; Date: 01-01-9999; Time: 10:00-11:00; Service: Full groom";
        assertCommandSuccess("delete-appointment 1", String.format(DeleteAppointmentCommand.MESSAGE_SUCCESS,
                appointmentDetails), new ModelManager());
    }

    @Test
    public void execute_clientIndexDeletionRequiresClientList() throws Exception {
        model.addClient(ALICE);

        logic.execute("list-pets");

        assertCommandException("delete-client 1", LogicManager.MESSAGE_INVALID_DISPLAY_TYPE);
    }

    @Test
    public void execute_petIndexDeletionRequiresPetList() throws Exception {
        model.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        model.addPet(pet);

        logic.execute("list-clients");
        assertCommandException("delete-pet 1", LogicManager.MESSAGE_INVALID_DISPLAY_TYPE);

        logic.execute("list-pets");
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deletePet(pet);
        assertCommandSuccess("delete-pet 1",
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, pet.getName()), expectedModel);
    }

    @Test
    public void execute_identifierDeletionDoesNotRequireMatchingDisplayedList() throws Exception {
        model.addClient(ALICE);
        Appointment appointment = createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        model.addAppointment(appointment);

        logic.execute("list-appointments");
        logic.execute("list-pets");
        CommandResult result = logic.execute("delete-appointment d/01-01-2000 t/10:30");

        assertEquals("Deleted appointment: Milo; Owner phone: " + ALICE.getPhone() + "; Date: 01-01-2000; "
                + "Time: 10:00-11:00; Service: Full groom", result.getFeedbackToUser());
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
    }

    //@@author khangsian0412
    @Test
    public void execute_deletePet_successAndSavesUpdatedBubuBook() throws Exception {
        Pet pet = new PetBuilder(AMY).build();
        model.addClient(AMY);
        model.addPet(pet);
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deletePet(pet);

        assertCommandSuccess("delete-pet p/Milo i/" + AMY.getPhone().value,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, pet.getName()), expectedModel);

        ReadOnlyBubuBook savedBubuBook = new JsonBubuBookStorage(
                temporaryFolder.resolve("bububook.json")).readBubuBook().get();
        assertEquals(expectedModel.getBubuBook(), savedBubuBook);
    }

    @Test
    public void execute_deletePetWithNoMatch_throwsCommandException() {
        assertCommandException("delete-pet p/Milo i/" + AMY.getPhone().value,
                DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_editPet_updatesAndSavesProfile() throws Exception {
        Pet pet = new PetBuilder(ALICE).withBreed("Golden retriever").build();
        model.addClient(ALICE);
        model.addPet(pet);
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        Pet editedPet = new PetBuilder(pet).withName("Milo Bean").withRequirement("Prefers quiet drying").build();
        expectedModel.setPet(pet, editedPet);

        assertCommandSuccess("edit-pet p/mIlO i/" + ALICE.getPhone().value
                        + " n/Milo Bean r/Prefers quiet drying",
                String.format(EditPetCommand.MESSAGE_SUCCESS, editedPet.getName()), expectedModel);

        ReadOnlyBubuBook savedBubuBook = new JsonBubuBookStorage(
                temporaryFolder.resolve("bububook.json")).readBubuBook().orElseThrow();
        assertEquals(expectedModel.getBubuBook(), savedBubuBook);
    }

    @Test
    public void execute_editPetSaveFails_restoresModel() {
        Pet pet = new PetBuilder(ALICE).build();
        model.addClient(ALICE);
        model.addPet(pet);
        JsonBubuBookStorage bubuBookStorage = new JsonBubuBookStorage(
                temporaryFolder.resolve("bububook.json")) {
            @Override
            public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(bubuBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        assertCommandException("edit-pet p/Milo i/" + ALICE.getPhone() + " r/Needs a short walk",
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }
    //@@author

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, LogicManager.MESSAGE_CLIENT_SAVE_FAILURE);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, LogicManager.MESSAGE_CLIENT_SAVE_FAILURE);
    }

    @Test
    public void execute_addPetSaveFails_restoresModel() {
        model.addClient(ALICE);
        model.addPet(new PetBuilder(ALICE).withBreed("Golden retriever").build());
        model.addAppointment(createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        JsonBubuBookStorage bubuBookStorage = new JsonBubuBookStorage(
                temporaryFolder.resolve("bububook.json")) {
            @Override
            public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        StorageManager storage = new StorageManager(bubuBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);

        assertCommandException("add-pet p/Mochi i/" + ALICE.getPhone() + " s/Dog r/Calm with baths",
                LogicManager.MESSAGE_UNABLE_TO_SAVE_PET);
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_addPetCorruptedRecords_reportsError() {
        logic = new LogicManager(model, new StorageManager(
                new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))), true);
        assertCommandException("add-pet p/Mochi i/91234567 s/Dog r/Calm with baths",
                LogicManager.MESSAGE_CORRUPTED_RECORDS);
    }

    @Test
    public void getFilteredClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredClientList().remove(0));
    }

    @Test
    public void accessors_delegateToModel() {
        assertSame(model.getFilteredClientList(), logic.getFilteredClientList());
        assertSame(model.getFilteredPetList(), logic.getFilteredPetList());
        assertSame(model.getFilteredAppointmentList(), logic.getFilteredAppointmentList());
        assertSame(model.getGuiSettings(), logic.getGuiSettings());

        GuiSettings guiSettings = new GuiSettings();
        logic.setGuiSettings(guiSettings);
        assertSame(guiSettings, model.getGuiSettings());
        assertSame(guiSettings, logic.getGuiSettings());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonBubuBookStorage that throws the IOException e when saving
        JsonBubuBookStorage bubuBookStorage = new JsonBubuBookStorage(prefPath) {
            @Override
            public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(bubuBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveBubuBook method by executing an add command
        String addCommand = AddClientCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    //@@author anderschow
    @Test
    public void execute_addClient_savesAndDisplaysClient() throws Exception {
        model.updateFilteredClientList(client -> false);
        CommandResult result = logic.execute("add-client n/Amelia Tan i/91234567 "
                + "e/amelia@example.com a/12 Punggol Drive t/regular");
        assertEquals("Client added: Amelia Tan (91234567).", result.getFeedbackToUser());
        assertEquals(1, model.getFilteredClientList().size());
        JsonBubuBookStorage saved = new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json"));
        assertEquals(model.getBubuBook(), saved.readBubuBook().orElseThrow());
    }
    //@@author

}
