package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyBubuBook;

/** Reads and writes all grooming records in one JSON file. */
public class JsonBubuBookStorage {
    private final Path filePath;

    public JsonBubuBookStorage(Path filePath) {
        this.filePath = requireNonNull(filePath);
    }

    public Path getBubuBookFilePath() {
        return filePath;
    }

    /** Reads all grooming records, or returns empty when the file does not exist. */
    public Optional<ReadOnlyBubuBook> readBubuBook() throws DataLoadingException {
        return readBubuBook(filePath);
    }

    /** Reads the complete book at the given path. */
    public Optional<ReadOnlyBubuBook> readBubuBook(Path path) throws DataLoadingException {
        requireNonNull(path);
        try {
            if (Files.notExists(path)) {
                return Optional.empty();
            }
            JsonSerializableBubuBook data = JsonUtil.fromJsonString(
                    Files.readString(path), JsonSerializableBubuBook.class);
            if (data == null) {
                throw new IllegalValueException("BubuBook must not be null.");
            }
            return Optional.of(data.toModelType());
        } catch (IOException | IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }

    /** Saves all clients, pets, and appointments together. */
    public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
        saveBubuBook(bubuBook, filePath);
    }

    /** Finishes writing before replacing the previous file to avoid truncating saved records on failure. */
    public void saveBubuBook(ReadOnlyBubuBook bubuBook, Path path) throws IOException {
        requireNonNull(bubuBook);
        requireNonNull(path);
        String json = JsonUtil.toJsonString(new JsonSerializableBubuBook(bubuBook));
        Path target = path.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "bububook-", ".tmp");
        try {
            Files.writeString(temporary, json);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
