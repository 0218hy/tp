package seedu.address.storage.appointment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.appointment.ReadOnlyAppointmentBook;

/**
 * Persistence operations for the appointment schedule.
 */
public interface AppointmentStorage {
    Path getAppointmentBookFilePath();

    /** Reads the schedule, returning empty when the file does not exist. */
    Optional<ReadOnlyAppointmentBook> readAppointmentBook() throws DataLoadingException;

    /** Saves the schedule, throwing IOException if it cannot be written. */
    void saveAppointmentBook(ReadOnlyAppointmentBook appointmentBook) throws IOException;
}
