package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientBook;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetBook;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;

/**
 * A JSON snapshot of separate client and pet books, preserving the legacy file format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableClientPetData {

    public static final String MESSAGE_DUPLICATE_CLIENT = "Clients list contains duplicate client(s).";
    public static final String MESSAGE_DUPLICATE_PET = "Pets list contains duplicate pet(s).";

    // Keep the original JSON key so existing files remain compatible.
    @JsonProperty("persons")
    private final List<JsonAdaptedClient> clients = new ArrayList<>();
    private final List<JsonAdaptedPet> pets = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableClientPetData} with the given serialized clients and pets.
     */
    @JsonCreator
    public JsonSerializableClientPetData(@JsonProperty("persons") List<JsonAdaptedClient> clients,
            @JsonProperty("pets") List<JsonAdaptedPet> pets) {
        this.clients.addAll(clients);
        if (pets != null) {
            this.pets.addAll(pets);
        }
    }

    /**
     * Converts a given {@code ClientPetData} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableClientPetData}.
     */
    public JsonSerializableClientPetData(ClientPetData source) {
        clients.addAll(source.getClientBook().getClientList().stream()
                .map(JsonAdaptedClient::new).collect(Collectors.toList()));
        pets.addAll(source.getPetBook().getPetList().stream().map(JsonAdaptedPet::new).collect(Collectors.toList()));
    }

    /**
     * Loads separate client and pet books, resolving pets to their loaded owners.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public ClientPetData toModelType() throws IllegalValueException {
        ClientBook clientBook = new ClientBook();
        PetBook petBook = new PetBook();
        for (JsonAdaptedClient jsonAdaptedClient : clients) {
            Client client = jsonAdaptedClient.toModelType();
            if (clientBook.hasClient(client)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CLIENT);
            }
            clientBook.addClient(client);
        }
        for (JsonAdaptedPet jsonAdaptedPet : pets) {
            Pet pet = jsonAdaptedPet.toModelType(clientBook.getClientList());
            if (petBook.hasPet(pet)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PET);
            }
            petBook.addPet(pet);
        }
        return new ClientPetData(clientBook, petBook);
    }

}
