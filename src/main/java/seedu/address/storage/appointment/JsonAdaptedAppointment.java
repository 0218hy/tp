package seedu.address.storage.appointment;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;

/**
 * Converts between appointment objects and simple fields that can be stored in JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonAdaptedAppointment {
    private final JsonAdaptedClient owner;
    private final JsonAdaptedPet pet;
    private final String ownerPhone;
    private final String petName;
    private final String date;
    private final String startTime;
    private final String endTime;
    private final String service;

    /** Constructs a snapshot, or a legacy record containing only participant identifiers. */
    @JsonCreator
    public JsonAdaptedAppointment(@JsonProperty("owner") JsonAdaptedClient owner,
            @JsonProperty("pet") JsonAdaptedPet pet, @JsonProperty("ownerPhone") String ownerPhone,
            @JsonProperty("petName") String petName, @JsonProperty("date") String date,
            @JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
            @JsonProperty("service") String service) {
        this.owner = owner;
        this.pet = pet;
        this.ownerPhone = ownerPhone;
        this.petName = petName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.service = service;
    }

    /** Constructs a legacy record for migration from the identifier-only format. */
    public JsonAdaptedAppointment(String ownerPhone, String petName, String date,
            String startTime, String endTime, String service) {
        this(null, null, ownerPhone, petName, date, startTime, endTime, service);
    }

    /** Copies participant snapshots so bookings survive removal of the registered records. */
    public JsonAdaptedAppointment(Appointment source) {
        this(new JsonAdaptedClient(source.getOwner()), new JsonAdaptedPet(source.getPet()), null, null,
                source.getDate().toString(), source.getStartTime().toString(), source.getEndTime().toString(),
                source.getService().toString());
    }

    /** Restores a self-contained appointment snapshot. Historical dates are allowed. */
    public Appointment toModelType() throws IllegalValueException {
        return toModelType(List.of(), List.of());
    }

    /**
     * Restores snapshots, reusing identical registered objects when available.
     * Legacy records require their participants to still exist because no profile details were saved.
     */
    public Appointment toModelType(List<Client> clients, List<Pet> pets) throws IllegalValueException {
        if (date == null || startTime == null || endTime == null || service == null) {
            throw new IllegalValueException("Appointment fields must not be missing.");
        }
        try {
            Client modelOwner;
            Pet modelPet;
            if (owner != null || pet != null) {
                if (owner == null || pet == null) {
                    throw new IllegalValueException("Appointment owner and pet snapshots must both be present.");
                }
                Client snapshotOwner = owner.toModelType();
                modelOwner = clients.stream().filter(snapshotOwner::equals).findFirst().orElse(snapshotOwner);
                Pet snapshotPet = pet.toModelType(List.of(modelOwner));
                modelPet = pets.stream().filter(snapshotPet::equals).findFirst().orElse(snapshotPet);
            } else {
                if (ownerPhone == null || petName == null) {
                    throw new IllegalValueException("Appointment fields must not be missing.");
                }
                Phone phone = new Phone(ownerPhone);
                PetName name = new PetName(petName.trim().replaceAll("\\s+", " "));
                List<Client> matches = clients.stream().filter(client -> client.getPhone().equals(phone)).toList();
                if (matches.size() != 1) {
                    throw new IllegalValueException("Legacy appointment owner does not identify a registered client.");
                }
                modelOwner = matches.get(0);
                modelPet = pets.stream().filter(candidate -> candidate.getOwner().equals(modelOwner)
                        && candidate.getName().getNormalizedName().equals(name.getNormalizedName()))
                        .findFirst().orElseThrow(() -> new IllegalValueException(
                                "Legacy appointment pet is not registered under its owner."));
            }
            return new Appointment(modelOwner, modelPet, new AppointmentDate(date),
                    new StartTime(startTime), new EndTime(endTime), Service.fromString(service));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }
}
