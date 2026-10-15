package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.BubuBook;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.storage.appointment.JsonAdaptedAppointment;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;

/**
 * A snapshot of all client, pet, and appointment records.
 */
@JsonRootName(value = "bububook")
class JsonSerializableBubuBook {

    public static final String MESSAGE_DUPLICATE_CLIENT = "Clients list contains duplicate client(s).";
    public static final String MESSAGE_DUPLICATE_PET = "Pets list contains duplicate pet(s).";

    private final List<JsonAdaptedClient> clients;
    private final List<JsonAdaptedPet> pets;
    private final List<JsonAdaptedAppointment> appointments;

    /** Constructs a complete serialized BubuBook. */
    @JsonCreator
    public JsonSerializableBubuBook(@JsonProperty("clients") List<JsonAdaptedClient> clients,
            @JsonProperty("pets") List<JsonAdaptedPet> pets,
            @JsonProperty("appointments") List<JsonAdaptedAppointment> appointments) {
        this.clients = clients == null ? null : new ArrayList<>(clients);
        this.pets = pets == null ? null : new ArrayList<>(pets);
        this.appointments = appointments == null ? null : new ArrayList<>(appointments);
    }

    /**
     * Converts a given {@code ReadOnlyBubuBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableBubuBook}.
     */
    public JsonSerializableBubuBook(ReadOnlyBubuBook source) {
        clients = source.getClientList().stream().map(JsonAdaptedClient::new).collect(Collectors.toList());
        pets = source.getPetList().stream().map(JsonAdaptedPet::new).collect(Collectors.toList());
        appointments = source.getAppointmentList().stream()
                .map(JsonAdaptedAppointment::new).collect(Collectors.toList());
    }

    /**
     * Restores the full book, resolving pet owners and rejecting invalid or overlapping records.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public BubuBook toModelType() throws IllegalValueException {
        if (clients == null || pets == null || appointments == null) {
            throw new IllegalValueException("Clients, pets, and appointments lists must be present.");
        }
        BubuBook bubuBook = new BubuBook();
        for (JsonAdaptedClient jsonAdaptedClient : clients) {
            if (jsonAdaptedClient == null) {
                throw new IllegalValueException("Client must not be null.");
            }
            Client client = jsonAdaptedClient.toModelType();
            if (bubuBook.hasClient(client)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CLIENT);
            }
            bubuBook.addClient(client);
        }
        for (JsonAdaptedPet jsonAdaptedPet : pets) {
            if (jsonAdaptedPet == null) {
                throw new IllegalValueException("Pet must not be null.");
            }
            Pet pet = jsonAdaptedPet.toModelType(bubuBook.getClientList());
            if (bubuBook.hasPet(pet)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PET);
            }
            bubuBook.addPet(pet);
        }
        for (JsonAdaptedAppointment appointment : appointments) {
            if (appointment == null) {
                throw new IllegalValueException("Appointment must not be null.");
            }
            try {
                bubuBook.addAppointment(appointment.toModelType());
            } catch (OverlappingAppointmentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        return bubuBook;
    }

}
