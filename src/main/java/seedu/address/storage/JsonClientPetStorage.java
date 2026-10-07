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

/**
 * A class to access client and pet data stored as a JSON file on the hard disk.
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
     * Returns client and pet data as a {@link ClientPetData}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ClientPetData> readClientPetData() throws DataLoadingException {
        return readClientPetData(filePath);
    }

    /**
     * Similar to {@link #readClientPetData()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ClientPetData> readClientPetData(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableClientPetData> jsonData = JsonUtil.readJsonFile(
                filePath, JsonSerializableClientPetData.class);
        if (!jsonData.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonData.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ClientPetData} to the storage.
     * @param data cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveClientPetData(ClientPetData data) throws IOException {
        saveClientPetData(data, filePath);
    }

    /**
     * Similar to {@link #saveClientPetData(ClientPetData)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveClientPetData(ClientPetData data, Path filePath) throws IOException {
        requireNonNull(data);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableClientPetData(data), filePath);
    }

}
