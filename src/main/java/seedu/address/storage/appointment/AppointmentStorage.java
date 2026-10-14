package seedu.address.storage.appointment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyGroomingBook;

/**
 * Persistence operations for the appointment schedule.
 */
public interface AppointmentStorage {
    Path getAppointmentFilePath();

    /** Reads a grooming book containing only appointments, or empty when the file does not exist. */
    Optional<ReadOnlyGroomingBook> readAppointmentData() throws DataLoadingException;

    /** Saves only the appointment schedule, throwing IOException if it cannot be written. */
    void saveAppointmentData(ReadOnlyGroomingBook appointmentBook) throws IOException;
}
