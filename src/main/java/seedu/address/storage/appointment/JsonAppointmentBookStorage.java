package seedu.address.storage.appointment;

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
import seedu.address.model.appointment.ReadOnlyAppointmentBook;

/**
 * Reads and writes appointments in a dedicated JSON file.
 */
public class JsonAppointmentBookStorage implements AppointmentStorage {
    private final Path filePath;

    public JsonAppointmentBookStorage(Path filePath) {
        this.filePath = requireNonNull(filePath);
    }

    @Override
    public Path getAppointmentBookFilePath() {
        return filePath;
    }

    @Override
    public Optional<ReadOnlyAppointmentBook> readAppointmentBook() throws DataLoadingException {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try {
            JsonSerializableAppointmentBook data = JsonUtil.fromJsonString(
                    Files.readString(filePath), JsonSerializableAppointmentBook.class);
            if (data == null) {
                throw new IllegalValueException("Appointment book must not be null.");
            }
            return Optional.of(data.toModelType());
        } catch (IOException | IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }

    @Override
    public void saveAppointmentBook(ReadOnlyAppointmentBook appointmentBook) throws IOException {
        requireNonNull(appointmentBook);
        String json = JsonUtil.toJsonString(new JsonSerializableAppointmentBook(appointmentBook));
        Path target = filePath.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "appointments-", ".tmp");
        try {
            // Finish writing before replacing the existing file, so a write failure cannot truncate it.
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
