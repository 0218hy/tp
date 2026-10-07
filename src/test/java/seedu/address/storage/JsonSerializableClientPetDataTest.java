package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.client.ClientBook;
import seedu.address.model.pet.PetBook;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

public class JsonSerializableClientPetDataTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableClientPetDataTest");
    private static final Path TYPICAL_CLIENTS_FILE = TEST_DATA_FOLDER.resolve("typicalClientsClientBook.json");
    private static final Path INVALID_CLIENT_FILE = TEST_DATA_FOLDER.resolve("invalidClientClientBook.json");
    private static final Path DUPLICATE_CLIENT_FILE = TEST_DATA_FOLDER.resolve("duplicateClientClientBook.json");

    @Test
    public void toModelType_typicalClientsFile_success() throws Exception {
        JsonSerializableClientPetData dataFromFile = JsonUtil.readJsonFile(TYPICAL_CLIENTS_FILE,
                JsonSerializableClientPetData.class).get();
        ClientPetData data = dataFromFile.toModelType();
        ClientBook typicalClientsClientBook = TypicalClients.getTypicalClientBook();
        assertEquals(data.getClientBook(), typicalClientsClientBook);
    }

    @Test
    public void toModelType_invalidClientFile_throwsIllegalValueException() throws Exception {
        JsonSerializableClientPetData dataFromFile = JsonUtil.readJsonFile(INVALID_CLIENT_FILE,
                JsonSerializableClientPetData.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateClients_throwsIllegalValueException() throws Exception {
        JsonSerializableClientPetData dataFromFile = JsonUtil.readJsonFile(DUPLICATE_CLIENT_FILE,
                JsonSerializableClientPetData.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableClientPetData.MESSAGE_DUPLICATE_CLIENT,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_petWithOwner_success() throws Exception {
        ClientBook clientBook = TypicalClients.getTypicalClientBook();
        PetBook petBook = new PetBook();
        petBook.addPet(new PetBuilder(TypicalClients.ALICE).build());
        ClientPetData data = new ClientPetData(clientBook, petBook);

        ClientPetData convertedData = new JsonSerializableClientPetData(data).toModelType();

        assertEquals(data, convertedData);
        assertSame(convertedData.getClientBook().getClientList().get(0),
                convertedData.getPetBook().getPetList().get(0).getOwner());
    }

    @Test
    public void toModelType_duplicatePets_throwsIllegalValueException() {
        JsonAdaptedClient owner = new JsonAdaptedClient(TypicalClients.ALICE);
        JsonAdaptedPet pet = new JsonAdaptedPet("Milo", TypicalClients.ALICE.getName().fullName, "DOG", "Daily walk");
        JsonSerializableClientPetData data = new JsonSerializableClientPetData(List.of(owner), List.of(pet, pet));

        assertThrows(IllegalValueException.class, JsonSerializableClientPetData.MESSAGE_DUPLICATE_PET,
                data::toModelType);
    }


    @Test
    public void legacyJson_loadsClientsAndPetsIntoSeparateBooks() throws Exception {
        String legacyJson = """
                {"persons": [{"name": "Alice", "phone": "91234567", "email": "alice@example.com",
                  "address": "1 Main Street", "tags": []}],
                 "pets": [{"name": "Milo", "ownerName": "Alice", "species": "DOG", "requirement": "Daily walk"}]}
                """;

        ClientPetData data = JsonUtil.fromJsonString(legacyJson, JsonSerializableClientPetData.class).toModelType();

        assertEquals(1, data.getClientBook().getClientList().size());
        assertEquals(1, data.getPetBook().getPetList().size());
        assertSame(data.getClientBook().getClientList().get(0), data.getPetBook().getPetList().get(0).getOwner());
        String savedJson = JsonUtil.toJsonString(new JsonSerializableClientPetData(data));
        assertTrue(savedJson.contains("\"persons\""));
        assertTrue(savedJson.contains("\"pets\""));
        assertFalse(savedJson.contains("\"clients\""));
        assertEquals(data, JsonUtil.fromJsonString(savedJson, JsonSerializableClientPetData.class).toModelType());
    }

    @Test
    public void snapshot_laterBookChanges_doNotChangeSavedData() {
        ClientBook clients = TypicalClients.getTypicalClientBook();
        PetBook pets = new PetBook();
        pets.addPet(new PetBuilder(TypicalClients.ALICE).build());
        ClientPetData snapshot = new ClientPetData(clients, pets);

        clients.removeClient(TypicalClients.ALICE);
        pets.setPets(List.of());

        assertTrue(snapshot.getClientBook().getClientList().contains(TypicalClients.ALICE));
        assertEquals(1, snapshot.getPetBook().getPetList().size());
    }
}
