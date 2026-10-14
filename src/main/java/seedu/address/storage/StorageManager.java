package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.GroomingBook;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.appointment.AppointmentStorage;
import seedu.address.storage.appointment.JsonAppointmentStorage;

/**
 * Manages storage of client and pet data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonClientPetStorage clientPetStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    private final AppointmentStorage appointmentStorage;

    /**
     * Creates a {@code StorageManager} with the given client/pet and user preference storage.
     */
    public StorageManager(JsonClientPetStorage clientPetStorage, JsonUserPrefsStorage userPrefsStorage) {
        this(clientPetStorage, userPrefsStorage, new JsonAppointmentStorage(
                clientPetStorage.getClientPetFilePath().resolveSibling("appointments.json")));
    }

    /** Creates storage with a separately configurable appointment file. */
    public StorageManager(JsonClientPetStorage clientPetStorage, JsonUserPrefsStorage userPrefsStorage,
            AppointmentStorage appointmentStorage) {
        this.clientPetStorage = clientPetStorage;
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


    // ================ Client and pet data methods ==============================

    @Override
    public Path getClientPetFilePath() {
        return clientPetStorage.getClientPetFilePath();
    }

    @Override
    public Optional<GroomingBook> readClientPetData() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + clientPetStorage.getClientPetFilePath());
        return clientPetStorage.readClientPetData();
    }

    @Override
    public void saveClientPetData(ReadOnlyGroomingBook data) throws IOException {
        logger.fine("Attempting to write to data file: " + clientPetStorage.getClientPetFilePath());
        clientPetStorage.saveClientPetData(data);
    }

}
