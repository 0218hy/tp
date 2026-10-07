package seedu.address.storage.appointment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.person.Phone;

/**
 * Converts between appointment objects and simple fields that can be stored in JSON.
 */
class JsonAdaptedAppointment {
    private final String ownerPhone;
    private final String petName;
    private final String date;
    private final String startTime;
    private final String endTime;
    private final String service;

    @JsonCreator
    public JsonAdaptedAppointment(@JsonProperty("ownerPhone") String ownerPhone,
            @JsonProperty("petName") String petName, @JsonProperty("date") String date,
            @JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
            @JsonProperty("service") String service) {
        this.ownerPhone = ownerPhone;
        this.petName = petName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.service = service;
    }

    public JsonAdaptedAppointment(Appointment source) {
        this(source.getOwnerPhone().toString(), source.getPetName(), source.getDate().toString(),
                source.getStartTime().toString(), source.getEndTime().toString(), source.getService().toString());
    }

    /** Restores a validated appointment. Historical dates are allowed. */
    public Appointment toModelType() throws IllegalValueException {
        if (ownerPhone == null || petName == null || date == null || startTime == null
                || endTime == null || service == null) {
            throw new IllegalValueException("Appointment fields must not be missing.");
        }
        try {
            return new Appointment(new Phone(ownerPhone), petName, new AppointmentDate(date),
                    new StartTime(startTime), new EndTime(endTime), Service.fromString(service));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }
}
