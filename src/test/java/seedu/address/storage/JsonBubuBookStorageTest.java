package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.HOON;
import static seedu.address.testutil.TypicalClients.IDA;
import static seedu.address.testutil.TypicalClients.getTypicalBubuBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.BubuBook;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

public class JsonBubuBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonBubuStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readBubuBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readBubuBook(null));
    }

    private java.util.Optional<ReadOnlyBubuBook> readBubuBook(String filePath) throws Exception {
        return new JsonBubuBookStorage(Paths.get(filePath)).readBubuBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readBubuBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readBubuBook("notJsonFormatBubu.json"));
    }

    @Test
    public void readBubuBook_invalidClientBubuBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readBubuBook("invalidPersonBubu.json"));
    }

    @Test
    public void readBubuBook_invalidAndValidClientBubuBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readBubuBook("invalidAndValidPersonBubu.json"));
    }

    @Test
    public void readAndSaveBubuBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempBubu.json");
        BubuBook original = getTypicalBubuBook();
        JsonBubuBookStorage jsonBubuBookStorage = new JsonBubuBookStorage(filePath);

        // Save in new file and read back
        jsonBubuBookStorage.saveBubuBook(original, filePath);
        ReadOnlyBubuBook readBack = jsonBubuBookStorage.readBubuBook(filePath).get();
        assertEquals(original, new BubuBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonBubuBookStorage.saveBubuBook(original, filePath);
        readBack = jsonBubuBookStorage.readBubuBook(filePath).get();
        assertEquals(original, new BubuBook(readBack));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonBubuBookStorage.saveBubuBook(original); // file path not specified
        readBack = jsonBubuBookStorage.readBubuBook().get(); // file path not specified
        assertEquals(original, new BubuBook(readBack));

    }

    @Test
    public void readAndSaveBubuBook_petBreedAndOwnerPhone_roundTrip() throws Exception {
        Path filePath = testFolder.resolve("PetsBubu.json");
        BubuBook original = getTypicalBubuBook();
        original.addPet(new PetBuilder(ALICE).withName("Mochi").withBreed("Golden retriever").build());
        JsonBubuBookStorage storage = new JsonBubuBookStorage(filePath);

        storage.saveBubuBook(original);
        ReadOnlyBubuBook readBack = storage.readBubuBook().orElseThrow();

        assertEquals(original, new BubuBook(readBack));
        assertEquals("Golden retriever", readBack.getPetList().getFirst().getBreed().value);
        assertEquals(ALICE.getPhone(), readBack.getPetList().getFirst().getOwner().getPhone());
    }

    @Test
    public void read_currentPetWithCanonicallyEquivalentOwnerName_loadsNormally() throws Exception {
        String decomposedName = "Jose\u0301 Tan";
        JsonAdaptedPet pet = new JsonAdaptedPet("Milo", decomposedName, TypicalClients.ALICE.getPhone().value,
                "DOG", null,
                "Daily brushing");

        ReadOnlyBubuBook loaded = readUnicodePetBook("current-unicode-pet.json", pet, decomposedName);

        assertEquals("Jos\u00e9 Tan", loaded.getClientList().getFirst().getName().fullName);
        assertEquals("Milo", loaded.getPetList().getFirst().getName().value);
        assertSame(loaded.getClientList().getFirst(), loaded.getPetList().getFirst().getOwner());
    }

    @Test
    public void read_legacyPetWithCanonicallyEquivalentOwnerName_loadsNormally() throws Exception {
        JsonAdaptedPet legacyPet = new JsonAdaptedPet("Milo", "Jos\u00e9 Tan", "DOG", "Daily brushing");

        ReadOnlyBubuBook loaded = readUnicodePetBook("legacy-unicode-pet.json", legacyPet, "Jose\u0301 Tan");

        assertEquals("Jos\u00e9 Tan", loaded.getClientList().getFirst().getName().fullName);
        assertEquals("Milo", loaded.getPetList().getFirst().getName().value);
        assertSame(loaded.getClientList().getFirst(), loaded.getPetList().getFirst().getOwner());
    }

    private ReadOnlyBubuBook readUnicodePetBook(String fileName, JsonAdaptedPet pet, String rawClientName)
            throws Exception {
        JsonAdaptedClient client = new JsonAdaptedClient(rawClientName, TypicalClients.ALICE.getPhone().value,
                TypicalClients.ALICE.getEmail().value, TypicalClients.ALICE.getAddress().value, null);
        JsonSerializableBubuBook data = new JsonSerializableBubuBook(List.of(client), List.of(pet), List.of());
        Path file = testFolder.resolve(fileName);
        Files.writeString(file, JsonUtil.toJsonString(data));

        return new JsonBubuBookStorage(file).readBubuBook().orElseThrow();
    }

    @Test
    public void saveBubuBook_nullBubuBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveBubuBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code bubuBook} at the specified {@code filePath}.
     */
    private void saveBubuBook(ReadOnlyBubuBook bubuBook, String filePath) {
        try {
            new JsonBubuBookStorage(Paths.get(filePath))
                    .saveBubuBook(bubuBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveBubuBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveBubuBook(new BubuBook(), null));
    }
}
