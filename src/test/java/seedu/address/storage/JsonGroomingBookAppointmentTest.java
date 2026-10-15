package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.GroomingBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;
import seedu.address.storage.appointment.JsonAdaptedAppointment;

public class JsonGroomingBookAppointmentTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndRead_historicalAppointments_preservesAllFieldsAndOrder() throws Exception {
        JsonGroomingBookStorage storage = storage("nested/bububook.json");
        GroomingBook book = new GroomingBook();
        book.addAppointment(create("10:00", "11:00"));
        book.addAppointment(create("11:00", "11:30"));
        storage.saveGroomingBook(book);
        assertEquals(book, storage.readGroomingBook().orElseThrow());
        String json = Files.readString(storage.getGroomingBookFilePath());
        assertTrue(json.contains("\"ownerPhone\" : \"91234567\""));
        assertTrue(json.contains("\"date\" : \"01-01-2000\""));
        assertTrue(json.contains("\"service\" : \"Full groom\""));
    }

    @Test
    public void saveGroomingBook_replacesPreviousScheduleAndCleansTemporaryFiles() throws Exception {
        JsonGroomingBookStorage storage = storage("bububook.json");
        GroomingBook book = new GroomingBook();
        book.addAppointment(create("10:00", "11:00"));
        storage.saveGroomingBook(book);
        storage.saveGroomingBook(new GroomingBook());
        assertEquals(new GroomingBook(), storage.readGroomingBook().orElseThrow());
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getGroomingBookFilePath()), paths.toList());
        }
    }

    @Test
    public void readGroomingBook_missingFile_returnsEmpty() throws Exception {
        assertTrue(storage("missing.json").readGroomingBook().isEmpty());
    }

    @Test
    public void readGroomingBook_malformedOrMissingData_rejectedWithoutChangingFile() throws Exception {
        JsonGroomingBookStorage storage = storage("invalid.json");
        for (String json : List.of("broken", "null", "[]", "{}", "{\"appointments\":null}",
                "{\"clients\":[],\"pets\":[],\"appointments\":[null]}",
                "{\"clients\":[],\"pets\":[],\"appointments\":[{}]}")) {
            Files.writeString(storage.getGroomingBookFilePath(), json);
            assertThrows(DataLoadingException.class, storage::readGroomingBook);
            assertEquals(json, Files.readString(storage.getGroomingBookFilePath()));
        }
    }

    @Test
    public void readGroomingBook_overlappingRecords_rejected() throws Exception {
        JsonGroomingBookStorage storage = storage("overlap.json");
        List<JsonAdaptedAppointment> records = List.of(new JsonAdaptedAppointment(create("10:00", "11:00")),
                new JsonAdaptedAppointment(create("10:30", "11:30")));
        JsonUtil.saveJsonFile(new JsonSerializableGroomingBook(List.of(), List.of(), records),
                storage.getGroomingBookFilePath());
        assertThrows(DataLoadingException.class, storage::readGroomingBook);
    }

    @Test
    public void toModelType_missingAndInvalidFields_rejected() {
        List<String> valid = List.of("91234567", "BuBu", "01-01-2000", "10:00", "11:00", "Full groom");
        List<String> invalid = List.of("abc", "!!!", "31-02-2000", "10:15", "09:30", "Unknown");
        for (int i = 0; i < valid.size(); i++) {
            List<String> fields = new ArrayList<>(valid);
            fields.set(i, null);
            assertThrows(IllegalValueException.class, () -> adapt(fields).toModelType());
            fields.set(i, invalid.get(i));
            assertThrows(IllegalValueException.class, () -> adapt(fields).toModelType());
        }
        assertThrows(IllegalValueException.class, () ->
                new JsonSerializableGroomingBook(List.of(), List.of(),
                        Arrays.asList((JsonAdaptedAppointment) null)).toModelType());
    }

    @Test
    public void saveGroomingBook_targetIsDirectory_reportsFailureAndCleansTemporaryFile() throws Exception {
        JsonGroomingBookStorage storage = storage("bububook.json");
        Files.createDirectory(storage.getGroomingBookFilePath());
        Path marker = storage.getGroomingBookFilePath().resolve("keep.txt");
        Files.writeString(marker, "keep");
        assertThrows(IOException.class, () -> storage.saveGroomingBook(new GroomingBook()));
        assertEquals("keep", Files.readString(marker));
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getGroomingBookFilePath()), paths.toList());
        }
    }

    @Test
    public void constructorAndSave_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new JsonGroomingBookStorage(null));
        assertThrows(NullPointerException.class, () -> storage("bububook.json").saveGroomingBook(null));
    }

    private JsonGroomingBookStorage storage(String filename) {
        return new JsonGroomingBookStorage(temporaryFolder.resolve(filename));
    }

    private JsonAdaptedAppointment adapt(List<String> fields) {
        return new JsonAdaptedAppointment(fields.get(0), fields.get(1), fields.get(2),
                fields.get(3), fields.get(4), fields.get(5));
    }

    private Appointment create(String start, String end) {
        return new Appointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}
