package seedu.address.model.appointment.exceptions;

/**
 * Signals that an operation would result in overlapping appointments.
 */
public class OverlappingAppointmentException extends RuntimeException {
    public OverlappingAppointmentException() {
        super("This appointment overlaps an existing appointment.");
    }
}
