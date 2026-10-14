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
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.testutil.PetBuilder;

public class JsonGroomingBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readGroomingBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readGroomingBook(null));
    }

    private java.util.Optional<ReadOnlyGroomingBook> readGroomingBook(String filePath) throws Exception {
        return new JsonGroomingBookStorage(Paths.get(filePath)).readGroomingBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readGroomingBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readGroomingBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readGroomingBook_invalidClientGroomingBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readGroomingBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readGroomingBook_invalidAndValidClientGroomingBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readGroomingBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveGroomingBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        GroomingBook original = getTypicalGroomingBook();
        JsonGroomingBookStorage jsonGroomingBookStorage = new JsonGroomingBookStorage(filePath);

        // Save in new file and read back
        jsonGroomingBookStorage.saveGroomingBook(original, filePath);
        ReadOnlyGroomingBook readBack = jsonGroomingBookStorage.readGroomingBook(filePath).get();
        assertEquals(original, new GroomingBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonGroomingBookStorage.saveGroomingBook(original, filePath);
        readBack = jsonGroomingBookStorage.readGroomingBook(filePath).get();
        assertEquals(original, new GroomingBook(readBack));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonGroomingBookStorage.saveGroomingBook(original); // file path not specified
        readBack = jsonGroomingBookStorage.readGroomingBook().get(); // file path not specified
        assertEquals(original, new GroomingBook(readBack));

    }

    @Test
    public void readAndSaveGroomingBook_petBreedAndOwnerPhone_roundTrip() throws Exception {
        Path filePath = testFolder.resolve("PetsAddressBook.json");
        GroomingBook original = getTypicalGroomingBook();
        original.addPet(new PetBuilder(ALICE).withName("Mochi").withBreed("Golden retriever").build());
        JsonGroomingBookStorage storage = new JsonGroomingBookStorage(filePath);

        storage.saveGroomingBook(original);
        ReadOnlyGroomingBook readBack = storage.readGroomingBook().orElseThrow();

        assertEquals(original, new GroomingBook(readBack));
        assertEquals("Golden retriever", readBack.getPetList().getFirst().getBreed().value);
        assertEquals(ALICE.getPhone(), readBack.getPetList().getFirst().getOwner().getPhone());
    }

    @Test
    public void saveGroomingBook_nullGroomingBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveGroomingBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code groomingBook} at the specified {@code filePath}.
     */
    private void saveGroomingBook(ReadOnlyGroomingBook groomingBook, String filePath) {
        try {
            new JsonGroomingBookStorage(Paths.get(filePath))
                    .saveGroomingBook(groomingBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveGroomingBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveGroomingBook(new GroomingBook(), null));
    }
}
