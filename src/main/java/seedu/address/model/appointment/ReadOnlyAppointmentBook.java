package seedu.address.model.appointment;

import javafx.collections.ObservableList;

/**
 * Unmodifiable view of an appointment book.
 */
public interface ReadOnlyAppointmentBook {
    /**
     * Returns an unmodifiable view of appointments in insertion order.
     * The list contains no null entries or overlapping appointments.
     */
    ObservableList<Appointment> getAppointmentList();
}
