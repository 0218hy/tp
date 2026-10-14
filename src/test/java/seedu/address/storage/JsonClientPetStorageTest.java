package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.HOON;
import static seedu.address.testutil.TypicalClients.IDA;
import static seedu.address.testutil.TypicalClients.getTypicalGroomingBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.GroomingBook;

public class JsonClientPetStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonClientPetStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readClientPetData_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readClientPetData(null));
    }

    private java.util.Optional<GroomingBook> readClientPetData(String filePath) throws Exception {
        return new JsonClientPetStorage(Paths.get(filePath)).readClientPetData(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readClientPetData("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("notJsonFormatClientBook.json"));
    }

    @Test
    public void readClientPetData_invalidClientGroomingBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("invalidClientClientBook.json"));
    }

    @Test
    public void readClientPetData_invalidAndValidClientGroomingBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("invalidAndValidClientClientBook.json"));
    }

    @Test
    public void readAndSaveGroomingBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempClientBook.json");
        GroomingBook original = getTypicalGroomingBook();
        JsonClientPetStorage jsonClientPetStorage = new JsonClientPetStorage(filePath);

        // Save in new file and read back
        jsonClientPetStorage.saveClientPetData(new GroomingBook(original), filePath);
        GroomingBook readBack = jsonClientPetStorage.readClientPetData(filePath).get();
        assertEquals(original, new GroomingBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonClientPetStorage.saveClientPetData(new GroomingBook(original), filePath);
        readBack = jsonClientPetStorage.readClientPetData(filePath).get();
        assertEquals(original, new GroomingBook(readBack));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonClientPetStorage.saveClientPetData(new GroomingBook(original)); // file path not specified
        readBack = jsonClientPetStorage.readClientPetData().get(); // file path not specified
        assertEquals(original, new GroomingBook(readBack));

    }

    @Test
    public void saveClientPetData_nullGroomingBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveClientPetData(null, "SomeFile.json"));
    }

    /**
     * Saves {@code groomingBook} at the specified {@code filePath}.
     */
    private void saveClientPetData(GroomingBook clientBook, String filePath) {
        try {
            new JsonClientPetStorage(Paths.get(filePath))
                    .saveClientPetData(clientBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveClientPetData_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                saveClientPetData(new GroomingBook(), null));
    }
}
