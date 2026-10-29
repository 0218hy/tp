package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
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
import seedu.address.model.pet.PetName;
import seedu.address.storage.JsonBubuBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

/** Exercises historical participant links through commands, persistence, and application restarts. */
public class HistoricalAppointmentIdentityTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private LogicManager logic;
    private Appointment original;

    @Test
    public void emailThenPhoneEdit_afterRestart_preservesHistoryAndClientDeletionLink() throws Exception {
        initialize();

        executeAndRestart("edit-client i/91234567 e/new@example.com a/456 New Road");
        assertEquals(original.getOwner(), appointment().getOwner());
        executeAndRestart("edit-client i/91234567 p/89998888");
        assertEquals("89998888", appointment().getOwnerPhone().value);
        assertEquals(original.getOwner().getEmail(), appointment().getOwner().getEmail());
        assertEquals(original.getOwner().getAddress(), appointment().getOwner().getAddress());

        executeAndRestart("delete-client i/89998888");
        assertEquals(new BubuBook(), model.getBubuBook());
    }

    @Test
    public void repeatedEmailEdits_afterRestart_preserveDirectClientDeletionLink() throws Exception {
        initialize();

        executeAndRestart("edit-client i/91234567 e/new@example.com");
        executeAndRestart("edit-client i/91234567 e/another@example.com");
        assertEquals(original.getOwner(), appointment().getOwner());
        executeAndRestart("delete-client i/91234567");

        assertEquals(new BubuBook(), model.getBubuBook());
    }

    @Test
    public void emailThenPetRename_afterRestart_preservesHistoryAndPetDeletionLink() throws Exception {
        initialize();

        executeAndRestart("edit-client i/91234567 e/new@example.com");
        executeAndRestart("edit-pet p/Mochi i/91234567 n/Rex r/New care instructions");
        assertEquals(new PetName("Rex"), appointment().getPetName());
        assertEquals(original.getPet().getRequirement(), appointment().getPet().getRequirement());
        assertEquals(original.getOwner(), appointment().getOwner());
        executeAndRestart("edit-client i/91234567 p/89998888");
        assertEquals("89998888", appointment().getOwnerPhone().value);
        executeAndRestart("delete-pet p/Rex i/89998888");

        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
        assertTrue(model.getBubuBook().getPetList().isEmpty());
        assertEquals(1, model.getBubuBook().getClientList().size());
    }

    @Test
    public void emailEdit_failedSave_restoresOwnerLinkAndAllowsRetry() throws Exception {
        initialize();
        BubuBook previous = new BubuBook(model.getBubuBook());
        String saved = Files.readString(bookPath());
        JsonBubuBookStorage failingStorage = new JsonBubuBookStorage(bookPath()) {
            @Override
            public void saveBubuBook(ReadOnlyBubuBook book) throws IOException {
                throw new IOException("Simulated write failure");
            }
        };
        LogicManager failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        assertThrows(CommandException.class, () ->
                failingLogic.execute("edit-client i/91234567 e/new@example.com"));

        assertEquals(previous, model.getBubuBook());
        assertEquals(saved, Files.readString(bookPath()));
        executeAndRestart("edit-client i/91234567 e/new@example.com");
        executeAndRestart("delete-client i/91234567");
        assertEquals(new BubuBook(), model.getBubuBook());
    }

    private void initialize() throws Exception {
        Client registered = new ClientBuilder().withName("Alex Tan").withPhone("91234567")
                .withEmail("alex@example.com").build();
        Pet pet = new PetBuilder(registered).withName("Mochi").build();
        original = new Appointment(registered, pet, new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        BubuBook book = new BubuBook();
        book.addClient(registered);
        book.addPet(pet);
        book.addAppointment(original);
        new JsonBubuBookStorage(bookPath()).saveBubuBook(book);
        restart();
    }

    private void executeAndRestart(String command) throws Exception {
        logic.execute(command);
        restart();
    }

    private void restart() throws Exception {
        JsonBubuBookStorage storage = new JsonBubuBookStorage(bookPath());
        model = new ModelManager(storage.readBubuBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private Path bookPath() {
        return temporaryFolder.resolve("bububook.json");
    }

    private Appointment appointment() {
        return model.getBubuBook().getAppointmentList().getFirst();
    }
}
