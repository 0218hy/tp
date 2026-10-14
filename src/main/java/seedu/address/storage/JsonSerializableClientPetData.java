package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.GroomingBook;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;

/**
 * A JSON snapshot of client and pet records from the grooming book, preserving the legacy file format.
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
     * Converts a given {@code GroomingBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableClientPetData}.
     */
    public JsonSerializableClientPetData(ReadOnlyGroomingBook source) {
        clients.addAll(source.getClientList().stream()
                .map(JsonAdaptedClient::new).collect(Collectors.toList()));
        pets.addAll(source.getPetList().stream().map(JsonAdaptedPet::new).collect(Collectors.toList()));
    }

    /**
     * Loads client and pet records from the grooming book, resolving pets to their loaded owners.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public GroomingBook toModelType() throws IllegalValueException {
        GroomingBook groomingBook = new GroomingBook();
        for (JsonAdaptedClient jsonAdaptedClient : clients) {
            Client client = jsonAdaptedClient.toModelType();
            if (groomingBook.hasClient(client)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CLIENT);
            }
            groomingBook.addClient(client);
        }
        for (JsonAdaptedPet jsonAdaptedPet : pets) {
            Pet pet = jsonAdaptedPet.toModelType(groomingBook.getClientList());
            if (groomingBook.hasPet(pet)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PET);
            }
            groomingBook.addPet(pet);
        }
        return groomingBook;
    }

}
