package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_CLIENTS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_CLIENT_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_CLIENT_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalClientsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_CLIENTS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalClientsAddressBook = TypicalClients.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalClientsAddressBook);
    }

    @Test
    public void toModelType_invalidClientFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_CLIENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateClients_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_CLIENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_CLIENT,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_petWithOwner_success() throws Exception {
        AddressBook addressBook = TypicalClients.getTypicalAddressBook();
        addressBook.addPet(new PetBuilder(TypicalClients.ALICE).build());

        AddressBook convertedAddressBook = new JsonSerializableAddressBook(addressBook).toModelType();

        assertEquals(addressBook, convertedAddressBook);
        assertSame(convertedAddressBook.getClientList().get(0), convertedAddressBook.getPetList().get(0).getOwner());
    }

    @Test
    public void toModelType_duplicatePets_throwsIllegalValueException() {
        JsonAdaptedClient owner = new JsonAdaptedClient(TypicalClients.ALICE);
        JsonAdaptedPet pet = new JsonAdaptedPet("Milo", TypicalClients.ALICE.getName().fullName, "DOG", "Daily walk");
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(owner), List.of(pet, pet));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PET, data::toModelType);
    }

}
