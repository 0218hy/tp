package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyGroomingBook;

/**
 * A class to access GroomingBook data stored as a JSON file on the hard disk.
 */
public class JsonClientPetStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonClientPetStorage.class);

    private Path filePath;

    public JsonClientPetStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getClientPetFilePath() {
        return filePath;
    }

    /**
     * Returns client and pet records in a grooming book with an empty appointment list.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyGroomingBook> readClientPetData() throws DataLoadingException {
        return readClientPetData(filePath);
    }

    /**
     * Similar to {@link #readClientPetData()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyGroomingBook> readClientPetData(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableClientPetData> jsonGroomingBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableClientPetData.class);
        if (!jsonGroomingBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonGroomingBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the client and pet records from the given grooming book.
     * @param groomingBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveClientPetData(ReadOnlyGroomingBook groomingBook) throws IOException {
        saveClientPetData(groomingBook, filePath);
    }

    /**
     * Similar to {@link #saveClientPetData(ReadOnlyGroomingBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveClientPetData(ReadOnlyGroomingBook groomingBook, Path filePath) throws IOException {
        requireNonNull(groomingBook);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableClientPetData(groomingBook), filePath);
    }

}
