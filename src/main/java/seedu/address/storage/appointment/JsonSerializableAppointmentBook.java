package seedu.address.storage.appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.AppointmentBook;
import seedu.address.model.appointment.ReadOnlyAppointmentBook;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;

/**
 * JSON representation of the complete appointment schedule.
 */
@JsonRootName(value = "appointmentbook")
class JsonSerializableAppointmentBook {
    private final List<JsonAdaptedAppointment> appointments;

    @JsonCreator
    public JsonSerializableAppointmentBook(@JsonProperty("appointments") List<JsonAdaptedAppointment> appointments) {
        this.appointments = appointments == null ? null : new ArrayList<>(appointments);
    }

    public JsonSerializableAppointmentBook(ReadOnlyAppointmentBook source) {
        appointments = source.getAppointmentList().stream()
                .map(JsonAdaptedAppointment::new).collect(Collectors.toList());
    }

    /** Restores the entire schedule, rejecting invalid records and overlapping slots. */
    public AppointmentBook toModelType() throws IllegalValueException {
        if (appointments == null) {
            throw new IllegalValueException("Appointments list is missing.");
        }
        AppointmentBook book = new AppointmentBook();
        for (JsonAdaptedAppointment appointment : appointments) {
            if (appointment == null) {
                throw new IllegalValueException("Appointment must not be null.");
            }
            try {
                book.addAppointment(appointment.toModelType());
            } catch (OverlappingAppointmentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        return book;
    }
}
