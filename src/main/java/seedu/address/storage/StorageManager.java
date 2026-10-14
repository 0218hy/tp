package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.appointment.AppointmentStorage;
import seedu.address.storage.appointment.JsonAppointmentStorage;

/**
 * Manages storage of GroomingBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonClientPetStorage groomingBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    private final AppointmentStorage appointmentStorage;

    /**
     * Creates a {@code StorageManager} with the given address book and user prefs storage.
     */
    public StorageManager(JsonClientPetStorage groomingBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this(groomingBookStorage, userPrefsStorage, new JsonAppointmentStorage(
                groomingBookStorage.getClientPetFilePath().resolveSibling("appointments.json")));
    }

    /** Creates storage with a separately configurable appointment file. */
    public StorageManager(JsonClientPetStorage groomingBookStorage, JsonUserPrefsStorage userPrefsStorage,
            AppointmentStorage appointmentStorage) {
        this.groomingBookStorage = groomingBookStorage;
        this.userPrefsStorage = userPrefsStorage;
        this.appointmentStorage = appointmentStorage;
    }

    // ================ Appointment methods ==============================

    @Override
    public Path getAppointmentFilePath() {
        return appointmentStorage.getAppointmentFilePath();
    }

    @Override
    public Optional<ReadOnlyGroomingBook> readAppointmentData() throws DataLoadingException {
        return appointmentStorage.readAppointmentData();
    }

    @Override
    public void saveAppointmentData(ReadOnlyGroomingBook appointmentBook) throws IOException {
        appointmentStorage.saveAppointmentData(appointmentBook);
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ GroomingBook methods ==============================

    @Override
    public Path getClientPetFilePath() {
        return groomingBookStorage.getClientPetFilePath();
    }

    @Override
    public Optional<ReadOnlyGroomingBook> readClientPetData() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + groomingBookStorage.getClientPetFilePath());
        return groomingBookStorage.readClientPetData();
    }

    @Override
    public void saveClientPetData(ReadOnlyGroomingBook groomingBook) throws IOException {
        logger.fine("Attempting to write to data file: " + groomingBookStorage.getClientPetFilePath());
        groomingBookStorage.saveClientPetData(groomingBook);
    }

}
