package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyBubuBook;

/** Reads and writes all grooming records in one JSON file. */
public class JsonBubuBookStorage {
    private static final Object SAVE_MONITOR = new Object();

    private final Path filePath;
    // Null contents represent a missing file; an absent key has not been successfully read or saved.
    private final Map<Path, byte[]> knownContents = new HashMap<>();

    //@@author 0218hy
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
    public synchronized Optional<ReadOnlyBubuBook> readBubuBook(Path path) throws DataLoadingException {
        requireNonNull(path);
        try {
            Path target = canonicalPath(path);
            knownContents.remove(target);
            byte[] contents = readContents(target);
            if (contents == null) {
                knownContents.put(target, null);
                return Optional.empty();
            }
            JsonSerializableBubuBook data = JsonUtil.fromJsonString(
                    StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(contents)).toString(),
                    JsonSerializableBubuBook.class);
            if (data == null) {
                throw new IllegalValueException("BubuBook must not be null.");
            }
            ReadOnlyBubuBook book = data.toModelType();
            knownContents.put(target, contents);
            return Optional.of(book);
        } catch (IOException | IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }

    /** Saves all clients, pets, and appointments together. */
    public void saveBubuBook(ReadOnlyBubuBook bubuBook) throws IOException {
        saveBubuBook(bubuBook, filePath);
    }

    /**
     * Saves only if the file still matches the last successful read or save for this storage instance.
     * A new file can be created without reading first. Existing files must have been read successfully.
     * The persistent sidecar lock serializes the comparison and replacement across BuBu processes.
     */
    public synchronized void saveBubuBook(ReadOnlyBubuBook bubuBook, Path path) throws IOException {
        requireNonNull(bubuBook);
        requireNonNull(path);
        byte[] contents = JsonUtil.toJsonString(new JsonSerializableBubuBook(bubuBook))
                .getBytes(StandardCharsets.UTF_8);
        Path target = canonicalPath(path);
        Files.createDirectories(target.getParent());
        // Avoid competing channels in one JVM: on some systems closing one releases that JVM's file locks.
        synchronized (SAVE_MONITOR) {
            saveUnderLock(target, contents);
        }
    }

    private void saveUnderLock(Path target, byte[] contents) throws IOException {
        Path lockPath = target.resolveSibling(target.getFileName() + ".lock");
        // Never delete this file: replacing its inode could let different processes hold different locks.
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = acquireLock(channel)) {
            byte[] current = readContents(target);
            if ((!knownContents.containsKey(target) && current != null)
                    || !Arrays.equals(knownContents.get(target), current)) {
                throw new RecordSaveConflictException(RecordSaveConflictException.MESSAGE_CHANGED);
            }
            replaceFile(target, contents);
            knownContents.put(target, contents);
        }
    }

    private FileLock acquireLock(FileChannel channel) throws IOException {
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                return lock;
            }
        } catch (OverlappingFileLockException e) {
            // Another storage instance in this JVM already holds the lock.
        }
        throw new RecordSaveConflictException(RecordSaveConflictException.MESSAGE_BUSY);
    }

    private byte[] readContents(Path target) throws IOException {
        try {
            return Files.readAllBytes(target);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    private Path canonicalPath(Path path) throws IOException {
        // Resolve aliases, including symlinked parent folders, even before the data file exists.
        return path.toFile().getCanonicalFile().toPath();
    }

    private void replaceFile(Path target, byte[] contents) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), "bububook-", ".tmp");
        try {
            Files.write(temporary, contents);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
    //@@author
}
