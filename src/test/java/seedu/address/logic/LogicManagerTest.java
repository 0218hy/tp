package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.client.ListClientsCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.pet.DeletePetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.storage.JsonClientPetStorage;
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
        JsonClientPetStorage groomingBookStorage =
                new JsonClientPetStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(groomingBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

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
        assertEquals("Mochi", model.getGroomingBook().getPetList().getFirst().getName().value);
        assertEquals(model.getGroomingBook(), new JsonClientPetStorage(temporaryFolder.resolve("addressBook.json"))
                .readClientPetData().orElseThrow());
    }

    @Test
    public void execute_deletePet_successAndSavesUpdatedGroomingBook() throws Exception {
        Pet pet = new PetBuilder(AMY).build();
        model.addClient(AMY);
        model.addPet(pet);
        Model expectedModel = new ModelManager(model.getGroomingBook(), new UserPrefs());
        expectedModel.deletePet(pet);

        assertCommandSuccess("delete-pet p/Milo i/" + AMY.getPhone().value,
                String.format(DeletePetCommand.MESSAGE_DELETE_PET_SUCCESS, pet.getName()), expectedModel);

        ReadOnlyGroomingBook savedGroomingBook = new JsonClientPetStorage(
                temporaryFolder.resolve("addressBook.json")).readClientPetData().get();
        assertEquals(expectedModel.getGroomingBook(), savedGroomingBook);
    }

    @Test
    public void execute_deletePetWithNoMatch_throwsCommandException() {
        assertCommandException("delete-pet p/Milo i/" + AMY.getPhone().value,
                DeletePetCommand.MESSAGE_PET_NOT_FOUND);
    }

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
        model.addAppointment(new Appointment(ALICE.getPhone(), "Milo", new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        Model expectedModel = new ModelManager(model.getGroomingBook(), new UserPrefs());
        JsonClientPetStorage groomingBookStorage = new JsonClientPetStorage(temporaryFolder.resolve("pets.json")) {
            @Override
            public void saveClientPetData(ReadOnlyGroomingBook groomingBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        StorageManager storage = new StorageManager(groomingBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);

        assertCommandException("add-pet p/Mochi i/" + ALICE.getPhone() + " s/Dog r/Calm with baths",
                LogicManager.MESSAGE_UNABLE_TO_SAVE_PET);
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_addPetCorruptedRecords_reportsError() {
        logic = new LogicManager(model, new StorageManager(
                new JsonClientPetStorage(temporaryFolder.resolve("pets.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))), true);
        assertCommandException("add-pet p/Mochi i/91234567 s/Dog r/Calm with baths",
                LogicManager.MESSAGE_CORRUPTED_RECORDS);
    }

    @Test
    public void getFilteredClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredClientList().remove(0));
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
        Model expectedModel = new ModelManager(model.getGroomingBook(), new UserPrefs());
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

        // Inject LogicManager with a JsonClientPetStorage that throws the IOException e when saving
        JsonClientPetStorage groomingBookStorage = new JsonClientPetStorage(prefPath) {
            @Override
            public void saveClientPetData(ReadOnlyGroomingBook groomingBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(groomingBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveClientPetData method by executing an add command
        String addCommand = AddClientCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Client expectedClient = new ClientBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addClient(expectedClient);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    @Test
    public void execute_addClient_savesAndDisplaysClient() throws Exception {
        model.updateFilteredClientList(client -> false);
        CommandResult result = logic.execute("add-client n/Amelia Tan i/91234567 "
                + "e/amelia@example.com a/12 Punggol Drive t/regular");
        assertEquals("Client added: Amelia Tan (91234567).", result.getFeedbackToUser());
        assertEquals(1, model.getFilteredClientList().size());
        JsonClientPetStorage saved = new JsonClientPetStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getGroomingBook(), saved.readClientPetData().orElseThrow());
    }

}
