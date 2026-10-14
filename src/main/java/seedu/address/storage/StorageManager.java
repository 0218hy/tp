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

/**
 * Manages storage of GroomingBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonGroomingBookStorage groomingBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    /**
     * Creates a storage manager for the shared grooming book and user preferences.
     */
    public StorageManager(JsonGroomingBookStorage groomingBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.groomingBookStorage = groomingBookStorage;
        this.userPrefsStorage = userPrefsStorage;
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
    public Path getGroomingBookFilePath() {
        return groomingBookStorage.getGroomingBookFilePath();
    }

    @Override
    public Optional<ReadOnlyGroomingBook> readGroomingBook() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + groomingBookStorage.getGroomingBookFilePath());
        return groomingBookStorage.readGroomingBook();
    }

    @Override
    public void saveGroomingBook(ReadOnlyGroomingBook groomingBook) throws IOException {
        logger.fine("Attempting to write to data file: " + groomingBookStorage.getGroomingBookFilePath());
        groomingBookStorage.saveGroomingBook(groomingBook);
    }

}
