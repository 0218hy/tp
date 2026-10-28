package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

//@@author anderschow
import java.nio.file.Files;
//@@author
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
//@@author anderschow
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
//@@author
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.BubuBook;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;
//@@author anderschow
import seedu.address.testutil.ClientBuilder;
//@@author
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

public class JsonSerializableBubuBookTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableBubuTest");
    private static final Path TYPICAL_CLIENTS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsBubu.json");
    private static final Path INVALID_CLIENT_FILE = TEST_DATA_FOLDER.resolve("invalidPersonBubu.json");
    private static final Path DUPLICATE_CLIENT_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonBubu.json");

    //@@author anderschow
    @TempDir
    public Path testFolder;

    @Test
    public void read_domainCaseConflict_rejectsWithoutChangingFile() throws Exception {
        JsonAdaptedClient first = new JsonAdaptedClient(new ClientBuilder()
                .withPhone("81112222").withEmail("alice@example.com").build());
        JsonAdaptedClient second = new JsonAdaptedClient(new ClientBuilder()
                .withPhone("82223333").withEmail("alice@EXAMPLE.COM").build());
        JsonSerializableBubuBook data = new JsonSerializableBubuBook(List.of(first, second), List.of(), List.of());
        assertThrows(IllegalValueException.class, JsonSerializableBubuBook.MESSAGE_DUPLICATE_CLIENT,
                data::toModelType);
        Path path = testFolder.resolve("conflicts.json");
        String original = JsonUtil.toJsonString(data);
        Files.writeString(path, original);
        assertThrows(DataLoadingException.class, () -> new JsonBubuBookStorage(path).readBubuBook());
        assertEquals(original, Files.readString(path));
    }

    @Test
    public void readAndSave_localPartCaseDistinct_preservesEmailSpelling() throws Exception {
        BubuBook book = new BubuBook();
        book.addClient(new ClientBuilder().withPhone("81112222").withEmail("alice@Example.COM").build());
        book.addClient(new ClientBuilder().withPhone("82223333").withEmail("Alice@EXAMPLE.com").build());
        JsonBubuBookStorage storage = new JsonBubuBookStorage(testFolder.resolve("emails.json"));
        storage.saveBubuBook(book);
        var restored = storage.readBubuBook().orElseThrow();
        assertEquals("alice@Example.COM", restored.getClientList().get(0).getEmail().value);
        assertEquals("Alice@EXAMPLE.com", restored.getClientList().get(1).getEmail().value);
    }
    //@@author


    @Test
    public void toModelType_typicalClientsFile_success() throws Exception {
        JsonSerializableBubuBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_CLIENTS_FILE,
                JsonSerializableBubuBook.class).get();
        BubuBook bubuBookFromFile = dataFromFile.toModelType();
        BubuBook typicalClientsBubuBook = TypicalClients.getTypicalBubuBook();
        assertEquals(bubuBookFromFile, typicalClientsBubuBook);
    }

    @Test
    public void toModelType_invalidClientFile_throwsIllegalValueException() throws Exception {
        JsonSerializableBubuBook dataFromFile = JsonUtil.readJsonFile(INVALID_CLIENT_FILE,
                JsonSerializableBubuBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateClients_throwsIllegalValueException() throws Exception {
        JsonSerializableBubuBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_CLIENT_FILE,
                JsonSerializableBubuBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableBubuBook.MESSAGE_DUPLICATE_CLIENT,
                dataFromFile::toModelType);
    }

    //@@author khangsian0412
    @Test
    public void toModelType_petWithOwner_success() throws Exception {
        BubuBook bubuBook = TypicalClients.getTypicalBubuBook();
        bubuBook.addPet(new PetBuilder(TypicalClients.ALICE).build());

        BubuBook convertedBubuBook = new JsonSerializableBubuBook(bubuBook).toModelType();

        assertEquals(bubuBook, convertedBubuBook);
        assertSame(convertedBubuBook.getClientList().get(0), convertedBubuBook.getPetList().get(0).getOwner());
    }

    @Test
    public void toModelType_duplicatePets_throwsIllegalValueException() {
        JsonAdaptedClient owner = new JsonAdaptedClient(TypicalClients.ALICE);
        JsonAdaptedPet pet = new JsonAdaptedPet("Milo", TypicalClients.ALICE.getName().fullName, "DOG", "Daily walk");
        JsonSerializableBubuBook data =
                new JsonSerializableBubuBook(List.of(owner), List.of(pet, pet), List.of());

        assertThrows(IllegalValueException.class, JsonSerializableBubuBook.MESSAGE_DUPLICATE_PET,
                data::toModelType);
    }
    //@@author

}
