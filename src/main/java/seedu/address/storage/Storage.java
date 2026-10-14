package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.appointment.AppointmentStorage;

/**
 * API of the Storage component
 */
public interface Storage extends AppointmentStorage {

    /**
     * Returns the file path of the UserPrefs data file.
     */
    Path getUserPrefsFilePath();

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    /**
     * Saves the given {@link seedu.address.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    /**
     * Returns the file path of the GroomingBook data file.
     */
    Path getClientPetFilePath();

    /**
     * Returns client and pet records in a grooming book with an empty appointment list.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    Optional<ReadOnlyGroomingBook> readClientPetData() throws DataLoadingException;

    /**
     * Saves the client and pet records from the given grooming book.
     * @param groomingBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveClientPetData(ReadOnlyGroomingBook groomingBook) throws IOException;

}
