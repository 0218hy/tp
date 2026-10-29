package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.storage.JsonBubuBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.appointment.JsonAdaptedAppointment;
import seedu.address.storage.client.JsonAdaptedClient;

/** Regression tests for malformed records that must activate startup write protection. */
public class RecordLoadingProtectionTest {
    private static final String ADD_CLIENT =
            "add-client n/Bob Tan i/82223333 e/bob@example.com a/123 Test Road";
    private static final String EMPTY_BOOK = "{\"clients\":[],\"pets\":[],\"appointments\":[]}";

    @TempDir
    public Path temporaryFolder;

    //@@author 0218hy
    @Test
    public void startup_nullClientTag_preservesFileAndBlocksMutation() throws Exception {
        String client = clientJson().replaceFirst("\"tags\"\\s*:\\s*\\[", "\"tags\": [null,");
        assertProtected(bookWithClient(client));
    }

    @Test
    public void startup_nullAppointmentOwnerTag_preservesFileAndBlocksMutation() throws Exception {
        String appointment = appointmentJson().replaceFirst("\"tags\"\\s*:\\s*\\[", "\"tags\": [null,");
        assertProtected(bookWithAppointment(appointment));
    }

    @Test
    public void startup_trailingContent_preservesFileAndBlocksMutation() throws Exception {
        for (String suffix : List.of("NOT VALID JSON", "{}", "[]", "null", "42")) {
            assertProtected(bookWithClient(clientJson()) + "\n" + suffix);
        }
    }

    @Test
    public void startup_secondBook_preservesFileAndBlocksMutation() throws Exception {
        // A populated second root must not be silently ignored after a valid empty book.
        assertProtected(EMPTY_BOOK + "\n" + bookWithClient(clientJson()));
    }

    @Test
    public void startup_duplicateRootLists_preservesFileAndBlocksMutation() throws Exception {
        for (String field : List.of("clients", "pets", "appointments")) {
            String valid = bookWithClient(clientJson());
            assertProtected(valid.substring(0, valid.length() - 1) + ",\"" + field + "\":[]}");
        }
    }

    @Test
    public void startup_duplicateNestedIdentity_preservesFileAndBlocksMutation() throws Exception {
        String client = clientJson();
        String duplicatePhone = client.replaceFirst("\\{", "{\"phone\":\"89998888\",");
        assertProtected(bookWithClient(duplicatePhone));

        String appointment = appointmentJson();
        assertProtected(bookWithAppointment(appointment.replaceFirst("\"owner\"\\s*:\\s*\\{",
                "\"owner\": {\"phone\":\"89998888\",")));
        assertProtected(bookWithAppointment(appointment.replaceFirst("\"pet\"\\s*:\\s*\\{",
                "\"pet\": {\"name\":\"Other pet\",")));
    }

    @Test
    public void startup_validBookWithWhitespace_loadsAndAllowsMutation() throws Exception {
        StorageManager storage = storage();
        Files.writeString(storage.getBubuBookFilePath(), bookWithClient(clientJson()) + " \r\n\t");
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        assertEquals(List.of(ALICE), model.getBubuBook().getClientList());
        assertFalse(isProtected(app));

        new LogicManager(model, storage, isProtected(app)).execute(ADD_CLIENT);

        assertEquals(2, storage.readBubuBook().orElseThrow().getClientList().size());
    }

    @Test
    public void startup_validAppointmentSnapshot_loadsRepeatedFieldsInSeparateObjects() throws Exception {
        StorageManager storage = storage();
        Files.writeString(storage.getBubuBookFilePath(), bookWithAppointment(appointmentJson()));
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        assertFalse(isProtected(app));
        assertEquals(1, model.getBubuBook().getAppointmentList().size());
        assertEquals(ALICE, model.getBubuBook().getAppointmentList().getFirst().getOwner());
    }

    private void assertProtected(String json) throws Exception {
        StorageManager storage = storage();
        Path path = storage.getBubuBookFilePath();
        byte[] original = json.getBytes(StandardCharsets.UTF_8);
        Files.write(path, original);
        assertThrows(DataLoadingException.class, storage::readBubuBook);

        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        assertEquals(new BubuBook(), model.getBubuBook());
        // Read the flag actually set by startup rather than assuming protection is enabled.
        assertTrue(isProtected(app));
        LogicManager logic = new LogicManager(model, storage, isProtected(app));
        for (String command : List.of(ADD_CLIENT, "clear")) {
            CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(LogicManager.MESSAGE_CORRUPTED_RECORDS, exception.getMessage());
            assertArrayEquals(original, Files.readAllBytes(path));
            assertEquals(new BubuBook(), model.getBubuBook());
        }
    }

    private boolean isProtected(MainApp app) throws Exception {
        Field field = MainApp.class.getDeclaredField("isBubuBookCorrupted");
        field.setAccessible(true);
        return field.getBoolean(app);
    }

    private StorageManager storage() {
        return new StorageManager(new JsonBubuBookStorage(temporaryFolder.resolve("bububook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private String clientJson() throws Exception {
        return JsonUtil.toJsonString(new JsonAdaptedClient(ALICE));
    }

    private String appointmentJson() throws Exception {
        return JsonUtil.toJsonString(new JsonAdaptedAppointment(createAppointment(ALICE, "BuBu",
                new AppointmentDate("01-01-2000"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM)));
    }

    private String bookWithClient(String client) {
        return "{\"clients\":[" + client + "],\"pets\":[],\"appointments\":[]}";
    }

    private String bookWithAppointment(String appointment) {
        return "{\"clients\":[],\"pets\":[],\"appointments\":[" + appointment + "]}";
    }
    //@@author
}
