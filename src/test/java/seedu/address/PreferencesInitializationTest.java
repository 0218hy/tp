package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.BubuBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

/** Tests preference validation and recovery through the application startup path. */
public class PreferencesInitializationTest {
    @TempDir
    public Path temporaryFolder;

    private JsonUserPrefsStorage storage;
    private Path path;

    //@@author 0218hy
    @BeforeEach
    public void setUp() {
        path = temporaryFolder.resolve("preferences.json");
        storage = new JsonUserPrefsStorage(path);
    }

    @Test
    public void readUserPrefs_rootNull_throwsDataLoadingException() throws Exception {
        Files.writeString(path, "null");
        assertThrows(DataLoadingException.class, storage::readUserPrefs);
    }

    @Test
    public void readUserPrefs_nullGuiSettings_throwsDataLoadingException() throws Exception {
        Files.writeString(path, "{\"guiSettings\":null}");
        assertThrows(DataLoadingException.class, storage::readUserPrefs);
    }

    @Test
    public void initPrefs_rootNull_usesAndSavesDefaults() throws Exception {
        Files.writeString(path, "null");
        assertInitialized(new UserPrefs());
    }

    @Test
    public void initPrefs_nullGuiSettings_usesAndSavesDefaults() throws Exception {
        Files.writeString(path, "{\"guiSettings\":null}");
        assertInitialized(new UserPrefs());
    }

    @Test
    public void initPrefs_invalidStructure_usesAndSavesDefaults() throws Exception {
        for (String json : List.of("[]", "42", "{\"guiSettings\":[]}", "{\"guiSettings\":\"invalid\"}",
                "{\"guiSettings\":{\"windowWidth\":{}}}", "broken")) {
            Files.writeString(path, json);
            assertInitialized(new UserPrefs());
        }
    }

    @Test
    public void initPrefs_missingFile_usesAndSavesDefaults() throws Exception {
        assertTrue(storage.readUserPrefs().isEmpty());
        assertInitialized(new UserPrefs());
    }

    @Test
    public void initPrefs_omittedSettings_usesDefaults() throws Exception {
        for (String json : List.of("{}", "{\"guiSettings\":{}}")) {
            Files.writeString(path, json);
            assertInitialized(new UserPrefs());
        }
    }

    @Test
    public void initPrefs_validSettings_preservesPreferences() throws Exception {
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(1000, 800, 300, 100));
        storage.saveUserPrefs(expected);
        assertInitialized(expected);
    }

    @Test
    public void initPrefs_nullWindowCoordinates_preservesValidDefaults() throws Exception {
        // Null coordinates mean no saved window position and remain valid.
        storage.saveUserPrefs(new UserPrefs());
        assertInitialized(new UserPrefs());
    }

    private void assertInitialized(UserPrefs expected) throws Exception {
        UserPrefs actual = new MainApp().initPrefs(storage);
        assertNotNull(actual.getGuiSettings());
        assertEquals(expected, actual);
        assertEquals(expected, storage.readUserPrefs().orElseThrow());
        assertEquals(expected, new ModelManager(new BubuBook(), actual).getUserPrefs());
        assertEquals(expected, new MainApp().initPrefs(storage));
    }
    //@@author
}
