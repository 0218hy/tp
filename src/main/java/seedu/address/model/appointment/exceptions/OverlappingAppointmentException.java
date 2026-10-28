package seedu.address.model.appointment.exceptions;

import static java.util.Objects.requireNonNull;

import seedu.address.model.appointment.Appointment;

/**
 * Signals that an operation would result in overlapping appointments.
 */
public class OverlappingAppointmentException extends RuntimeException {
    private final Appointment conflictingAppointment;

    //@@author 0218hy
    /** Creates an overlap error identifying the existing conflicting appointment. */
    public OverlappingAppointmentException(Appointment conflictingAppointment) {
        super("This appointment overlaps an existing appointment.");
        this.conflictingAppointment = requireNonNull(conflictingAppointment);
    }

    /** Returns the existing appointment that conflicts with the attempted addition. */
    public Appointment getConflictingAppointment() {
        return conflictingAppointment;
    }
    //@@author
}
