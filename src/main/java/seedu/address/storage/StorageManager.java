package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * Manages storage of BubuBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonBubuBookStorage bubuBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    /**
     * Creates a storage manager for the shared BubuBook and user preferences.
     */
    public StorageManager(JsonBubuBookStorage bubuBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.bubuBookStorage = bubuBookStorage;
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


    // ================ BubuBook methods ==============================

    @Override
    public Path getBubuBookFilePath() {
        return bubuBookStorage.getBubuBookFilePath();
    }

    @Override
    public Optional<ReadOnlyBubuBook> readBubuBook() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + bubuBookStorage.getBubuBookFilePath());
        return bubuBookStorage.readBubuBook();
    }

    @Override
    public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
        logger.fine("Attempting to write to data file: " + bubuBookStorage.getBubuBookFilePath());
        bubuBookStorage.saveBubuBook(bubuBook);
    }

}
