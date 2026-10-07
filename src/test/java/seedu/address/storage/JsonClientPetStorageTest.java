package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.HOON;
import static seedu.address.testutil.TypicalClients.IDA;
import static seedu.address.testutil.TypicalClients.getTypicalClientBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.client.ClientBook;
import seedu.address.model.pet.PetBook;

public class JsonClientPetStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonClientPetStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readClientPetData_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readClientPetData(null));
    }

    private java.util.Optional<ClientPetData> readClientPetData(String filePath) throws Exception {
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
    public void readClientPetData_invalidClientClientBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("invalidClientClientBook.json"));
    }

    @Test
    public void readClientPetData_invalidAndValidClientClientBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("invalidAndValidClientClientBook.json"));
    }

    @Test
    public void readAndSaveClientBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempClientBook.json");
        ClientBook original = getTypicalClientBook();
        JsonClientPetStorage jsonClientPetStorage = new JsonClientPetStorage(filePath);

        // Save in new file and read back
        jsonClientPetStorage.saveClientPetData(new ClientPetData(original, new PetBook()), filePath);
        ClientPetData readBack = jsonClientPetStorage.readClientPetData(filePath).get();
        assertEquals(original, new ClientBook(readBack.getClientBook()));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonClientPetStorage.saveClientPetData(new ClientPetData(original, new PetBook()), filePath);
        readBack = jsonClientPetStorage.readClientPetData(filePath).get();
        assertEquals(original, new ClientBook(readBack.getClientBook()));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonClientPetStorage.saveClientPetData(new ClientPetData(original, new PetBook())); // file path not specified
        readBack = jsonClientPetStorage.readClientPetData().get(); // file path not specified
        assertEquals(original, new ClientBook(readBack.getClientBook()));

    }

    @Test
    public void saveClientPetData_nullClientBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveClientPetData(null, "SomeFile.json"));
    }

    /**
     * Saves {@code clientBook} at the specified {@code filePath}.
     */
    private void saveClientPetData(ClientPetData clientBook, String filePath) {
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
                saveClientPetData(new ClientPetData(new ClientBook(), new PetBook()), null));
    }
}
