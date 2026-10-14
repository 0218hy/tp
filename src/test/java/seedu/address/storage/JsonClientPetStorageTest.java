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

public class JsonClientPetStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readGroomingBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readClientPetData(null));
    }

    private java.util.Optional<ReadOnlyGroomingBook> readClientPetData(String filePath) throws Exception {
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
        assertThrows(DataLoadingException.class, () -> readClientPetData("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readGroomingBook_invalidClientGroomingBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("invalidPersonAddressBook.json"));
    }

    @Test
    public void readGroomingBook_invalidAndValidClientGroomingBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientPetData("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveGroomingBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        GroomingBook original = getTypicalGroomingBook();
        JsonClientPetStorage jsonGroomingBookStorage = new JsonClientPetStorage(filePath);

        // Save in new file and read back
        jsonGroomingBookStorage.saveClientPetData(original, filePath);
        ReadOnlyGroomingBook readBack = jsonGroomingBookStorage.readClientPetData(filePath).get();
        assertEquals(original, new GroomingBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonGroomingBookStorage.saveClientPetData(original, filePath);
        readBack = jsonGroomingBookStorage.readClientPetData(filePath).get();
        assertEquals(original, new GroomingBook(readBack));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonGroomingBookStorage.saveClientPetData(original); // file path not specified
        readBack = jsonGroomingBookStorage.readClientPetData().get(); // file path not specified
        assertEquals(original, new GroomingBook(readBack));

    }

    @Test
    public void readAndSaveGroomingBook_petBreedAndOwnerPhone_roundTrip() throws Exception {
        Path filePath = testFolder.resolve("PetsAddressBook.json");
        GroomingBook original = getTypicalGroomingBook();
        original.addPet(new PetBuilder(ALICE).withName("Mochi").withBreed("Golden retriever").build());
        JsonClientPetStorage storage = new JsonClientPetStorage(filePath);

        storage.saveClientPetData(original);
        ReadOnlyGroomingBook readBack = storage.readClientPetData().orElseThrow();

        assertEquals(original, new GroomingBook(readBack));
        assertEquals("Golden retriever", readBack.getPetList().getFirst().getBreed().value);
        assertEquals(ALICE.getPhone(), readBack.getPetList().getFirst().getOwner().getPhone());
    }

    @Test
    public void saveGroomingBook_nullGroomingBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveClientPetData(null, "SomeFile.json"));
    }

    /**
     * Saves {@code groomingBook} at the specified {@code filePath}.
     */
    private void saveClientPetData(ReadOnlyGroomingBook groomingBook, String filePath) {
        try {
            new JsonClientPetStorage(Paths.get(filePath))
                    .saveClientPetData(groomingBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveGroomingBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveClientPetData(new GroomingBook(), null));
    }
}
